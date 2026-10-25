package com.otus.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDragScope
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.theme.LocalMotionScale
import com.otus.designsystem.theme.Motion
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.SwipeTokens
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Hosts a row that reveals [startToEnd] when dragged toward the end edge and [endToStart] when
 * dragged toward the start edge. An accent card the row's size waits behind it, so the row slides
 * off it; give [content] an opaque background, such as a [CompactContentRow] card. The action's icon grows in
 * beside its label until the fixed threshold, where a haptic marks that release will act.
 * Releasing past it flashes and runs the action once; shorter drags spring back. The row stops
 * once its action is fully revealed, and drags cannot move it again until it is still. A null
 * action leaves that direction still, and a horizontal drag never reaches the row as a tap.
 * Accessibility services offer both actions as custom actions.
 */
@Composable
public fun SwipeActions(
    startToEnd: SwipeAction?,
    endToStart: SwipeAction?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val threshold = with(LocalDensity.current) { SwipeTokens.threshold.toPx() }
    val motionScale = LocalMotionScale.current
    var width by remember { mutableIntStateOf(0) }
    val state = rememberSwipeState(width, startToEnd != null, endToStart != null)
    val flash = remember { Animatable(0f) }
    // Keeps the action a release ran on the backdrop until the row is back at rest, even if the row's
    // actions change. While it is set, no release can run another action.
    var running by remember { mutableStateOf<RunningSwipe?>(null) }
    val scope = rememberCoroutineScope()
    val currentStartToEnd by rememberUpdatedState(startToEnd)
    val currentEndToStart by rememberUpdatedState(endToStart)
    val fling =
        remember(state, threshold, motionScale) {
            thresholdFlingBehavior(state, threshold, motionScale) { side ->
                val action = side.pick(currentStartToEnd, currentEndToStart)?.takeIf { running == null }
                action?.let {
                    // Set before the launch, so a second release in the same frame cannot also commit.
                    running = RunningSwipe(side, it)
                    scope.launch {
                        try {
                            runSwipe(state, side, it, flash, motionScale)
                        } finally {
                            running = null
                        }
                    }
                }
                action != null
            }
        }
    val revealWidths = remember { mutableStateMapOf<SwipeAnchor, Float>() }
    val actionFor = { anchor: SwipeAnchor? -> anchor.pick(startToEnd, endToStart) }
    val offset = state.requireOffset()
    val direction = running?.side ?: directionOf(offset)
    ThresholdHaptic(armed = abs(offset) >= threshold && actionFor(directionOf(offset)) != null)
    Box(
        modifier
            .onSizeChanged { width = it.width }
            // Always claims horizontal drags, so a swipe toward a side without an action is not a tap.
            .anchoredDraggable(state, Orientation.Horizontal, flingBehavior = fling)
            .swipeAccessibility(startToEnd, endToStart),
    ) {
        val revealed = revealWidths[directionOf(offset)] ?: threshold
        val rowOffset = boundedRevealOffset(offset, revealed)
        (running?.action ?: actionFor(direction))?.let { action ->
            SwipeBackdrop(
                action,
                abs(offset),
                direction == SwipeAnchor.StartToEnd,
                flash.value,
                Modifier.matchParentSize(),
            ) {
                revealWidths[direction] = maxOf(threshold, it)
            }
        }
        SwipeForeground(rowOffset, content)
    }
}

/** The drag state, with anchors at the row's full width on each side that has an action. */
@Composable
private fun rememberSwipeState(
    width: Int,
    startToEnd: Boolean,
    endToStart: Boolean,
): AnchoredDraggableState<SwipeAnchor> {
    val state =
        remember {
            AnchoredDraggableState(SwipeAnchor.Settled, DraggableAnchors { SwipeAnchor.Settled at 0f })
        }
    val anchors =
        remember(width, startToEnd, endToStart) {
            DraggableAnchors {
                if (width > 0 && endToStart) SwipeAnchor.EndToStart at -width.toFloat()
                SwipeAnchor.Settled at 0f
                if (width > 0 && startToEnd) SwipeAnchor.StartToEnd at width.toFloat()
            }
        }
    SideEffect {
        val target = state.settledValue.takeIf { anchors.hasPositionFor(it) } ?: SwipeAnchor.Settled
        state.updateAnchors(anchors, newTarget = target)
    }
    return state
}

/** The action a release past the threshold is running, and the side it was revealed on. */
private class RunningSwipe(
    val side: SwipeAnchor,
    val action: SwipeAction,
)

/**
 * Runs [action] after a release toward [side]. The row slides to that side's anchor while the
 * action flashes. A dismissing action runs after its flash and leaves the row for its list to
 * remove; if the row is still there after [SwipeTokens.DISMISS_RESTORE_MILLIS], for example because
 * removal failed, it returns to rest. Any other action runs at once and the row returns to rest.
 * The slides outrank user drags, so a touch cannot stop the row halfway and release it past the
 * threshold a second time.
 */
internal suspend fun runSwipe(
    state: AnchoredDraggableState<SwipeAnchor>,
    side: SwipeAnchor,
    action: SwipeAction,
    flash: Animatable<Float, *>,
    motionScale: Float,
) = coroutineScope {
    launch {
        flash.snapTo(SwipeTokens.FLASH_ALPHA)
        if (!action.dismisses) action.onSwipe()
        flash.animateTo(0f, Motion.tween(scale = motionScale))
        // Running a dismissing action removes the row, so it waits until the flash has faded.
        if (action.dismisses) action.onSwipe()
    }
    state.anchoredDrag(MutatePriority.PreventUserInput) { anchors ->
        slideTo(state.requireOffset(), anchors.positionOf(side), motionScale)
        if (!action.dismisses) slideTo(state.requireOffset(), anchors.positionOf(SwipeAnchor.Settled), motionScale)
    }
    if (action.dismisses) {
        delay(SwipeTokens.DISMISS_RESTORE_MILLIS)
        state.anchoredDrag(MutatePriority.PreventUserInput) { anchors ->
            slideTo(state.requireOffset(), anchors.positionOf(SwipeAnchor.Settled), motionScale)
        }
    }
}

private suspend fun AnchoredDragScope.slideTo(
    from: Float,
    to: Float,
    motionScale: Float,
) {
    if (to.isNaN()) return
    animate(from, to, animationSpec = Motion.tween(MotionSpeed.Relaxed, scale = motionScale)) { value, _ ->
        dragTo(value)
    }
}

/** A haptic each time a drag crosses the point where releasing will act. */
@Composable
internal fun ThresholdHaptic(armed: Boolean) {
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(armed) { if (armed) haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate) }
}

/** The row itself, moved by [offset]; opaque so the revealed area never shows through it. */
@Composable
private fun SwipeForeground(
    offset: Float,
    content: @Composable () -> Unit,
) {
    val sign = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
    Box(
        Modifier
            .fillMaxWidth()
            .offset { IntOffset((offset * sign).roundToInt(), 0) },
    ) { content() }
}

/** Offers the row's swipes as custom actions, so they work without the gesture. */
private fun Modifier.swipeAccessibility(vararg actions: SwipeAction?): Modifier =
    semantics {
        customActions =
            actions.filterNotNull().map { action ->
                CustomAccessibilityAction(action.label) {
                    action.onSwipe()
                    true
                }
            }
    }
