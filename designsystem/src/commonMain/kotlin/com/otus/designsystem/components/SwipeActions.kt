package com.otus.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.zIndex
import com.otus.designsystem.theme.MotionTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SwipeTokens
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Hosts a row that reveals [startToEnd] when dragged toward the end edge and [endToStart] when
 * dragged toward the start edge. The uncovered area is an accent shape that grows out of the row's
 * edge, joined to it by concave corners and rounded at its free end. The action's icon grows in
 * beside its label until the fixed threshold, where a haptic marks that release will act.
 * Releasing past it flashes and runs the action; shorter drags spring back. The row stops once
 * its action is fully revealed. A null action leaves that
 * direction still, and a horizontal drag never reaches the row as a tap. Accessibility services
 * offer both actions as custom actions.
 */
@Composable
public fun SwipeActions(
    startToEnd: SwipeAction?,
    endToStart: SwipeAction?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val threshold = with(LocalDensity.current) { SwipeTokens.threshold.toPx() }
    var width by remember { mutableIntStateOf(0) }
    val state = rememberSwipeState(width, startToEnd != null, endToStart != null)
    // Set the moment a release passes the threshold, so the action and its flash do not wait for the row.
    var committed by remember { mutableStateOf<SwipeAnchor?>(null) }
    val fling = remember(state, threshold) { thresholdFlingBehavior(state, threshold) { committed = it } }
    val flash = remember { Animatable(0f) }
    // Keeps the committed action's backdrop while the row returns through rest.
    var shown by remember { mutableStateOf<SwipeAnchor?>(null) }
    val revealWidths = remember { mutableStateMapOf<SwipeAnchor, Float>() }
    val actionFor = { anchor: SwipeAnchor? -> anchor.pick(startToEnd, endToStart) }
    RunCommittedAction(state, committed, flash, actionFor) { anchor ->
        shown = anchor
        if (anchor == null) committed = null
    }
    val offset = state.requireOffset()
    val direction = shown ?: directionOf(offset)
    ThresholdHaptic(armed = abs(offset) >= threshold && actionFor(directionOf(offset)) != null)
    Box(
        modifier
            .onSizeChanged { width = it.width }
            // Always claims horizontal drags, so a swipe toward a side without an action is not a tap.
            .anchoredDraggable(state, Orientation.Horizontal, flingBehavior = fling)
            // A swiped row's revealed corners reach into its neighbors, so it draws above them.
            .zIndex(if (direction == SwipeAnchor.Settled) 0f else 1f)
            .swipeAccessibility(startToEnd, endToStart),
    ) {
        val revealed = revealWidths[directionOf(offset)] ?: threshold
        val rowOffset = boundedRevealOffset(offset, revealed)
        actionFor(direction)?.let { action ->
            SwipeBackdrop(
                action,
                abs(offset),
                abs(rowOffset),
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

/**
 * Runs the action a release committed to, with a flash, as soon as the finger lifts past the
 * threshold. An action that keeps the row runs at once and the row returns once it has reached
 * its anchor. A dismissing action flashes first, then runs, and leaves the row for its list to
 * remove.
 */
@Composable
private fun RunCommittedAction(
    state: AnchoredDraggableState<SwipeAnchor>,
    committed: SwipeAnchor?,
    flash: Animatable<Float, *>,
    actionFor: (SwipeAnchor?) -> SwipeAction?,
    onShown: (SwipeAnchor?) -> Unit,
) {
    LaunchedEffect(committed) {
        val direction = committed ?: return@LaunchedEffect
        val action = actionFor(direction) ?: return@LaunchedEffect
        onShown(direction)
        flash.snapTo(SwipeTokens.FLASH_ALPHA)
        val fade = tween<Float>(SwipeTokens.FLASH_MILLIS, easing = MotionTokens.standard)
        if (action.dismisses) {
            flash.animateTo(0f, fade)
            action.onSwipe()
        } else {
            action.onSwipe()
            coroutineScope {
                launch { flash.animateTo(0f, fade) }
                launch {
                    snapshotFlow { state.settledValue }.first { it == direction }
                    state.animateTo(SwipeAnchor.Settled)
                }
            }
        }
        onShown(null)
    }
}

/** A haptic each time a drag crosses the point where releasing will act. */
@Composable
private fun ThresholdHaptic(armed: Boolean) {
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
            .offset { IntOffset((offset * sign).roundToInt(), 0) }
            .background(OtusColors.bgRegular),
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
