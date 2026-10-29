package com.otus.designsystem.shared.feedback.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.util.lerp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.otus.designsystem.shared.feedback.elements.Toast
import com.otus.designsystem.shared.feedback.elements.ToastAction
import com.otus.designsystem.shared.pages.components.aboveBottomBars
import com.otus.designsystem.shared.panels.components.drawEdgeFade
import com.otus.designsystem.shared.swipe.components.SwipeToDismiss
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.tokens.feedback.ToastTokens
import com.otus.designsystem.theme.tokens.overlays.ActionBarTokens
import kotlinx.coroutines.flow.collectLatest

/**
 * Shows the toasts in [state] as a stack: the newest in front and up to two older ones peeking
 * out behind it, each with its own timer. Tapping the stack fans it out into a list and pauses
 * every timer; tapping again or system back stacks it again. Any toast in front, or any toast in
 * the list, can be swiped away in either direction; dismissing one with an action leaves the action
 * done. A toast whose time is up or whose action ran sinks, shrinks, and fades on the exit curve,
 * behind the bar when there is one; a new one rises the same way on the enter curve. A swiped
 * toast slides off the screen and fades out over the bar inset at the screen's sides. The group
 * is as wide as its widest toast. Align the host to the bottom of the window with [modifier]; it
 * spans the window's width and rises above any bar in
 * [LocalBottomClearance][com.otus.designsystem.shared.pages.components.LocalBottomClearance].
 */
@Composable
public fun ToastHost(
    state: ToastState,
    modifier: Modifier = Modifier,
) {
    RunTimers(state)
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = state.expanded,
        onBackCompleted = { state.expanded = false },
    )
    // 0 while the toasts are stacked, 1 once they fan out into a list.
    val fan by animateFloatAsState(if (state.expanded) 1f else 0f, motionTween(MotionSpeed.Normal), label = "Toast fan")
    Box(modifier.fillMaxWidth().screenEdgeFade().aboveBottomBars(), contentAlignment = Alignment.BottomCenter) {
        ToastGroup(state) { fan }
    }
}

/**
 * The toasts themselves, as wide as the widest one, as far from the screen's sides as the bar.
 * They are placed by their measured heights in one layout pass, so a toast whose text wraps onto
 * more lines keeps the ones behind it peeking out above it while stacked, and below one another
 * once fanned out. Stacked, the toasts behind take the front one's height, so only their top edges
 * show; [fan] moves each to its own height and place in the list.
 */
@Composable
private fun ToastGroup(
    state: ToastState,
    fan: () -> Float,
) {
    val count = state.entries.size
    // As far from the sides as the action bar, which matches its gap to the bottom of the screen.
    val navigation = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Layout(
        content = {
            state.entries.forEachIndexed { index, entry ->
                key(entry.id) { ToastItem(state, entry, depth = count - 1 - index, fan) }
            }
        },
        modifier = Modifier.padding(horizontal = ActionBarTokens.barInset + navigation),
    ) { measurables, constraints ->
        if (measurables.isEmpty()) return@Layout layout(0, 0) {}
        val progress = fan()
        val width = measurables.maxOf { it.maxIntrinsicWidth(Constraints.Infinity) }.coerceAtMost(constraints.maxWidth)
        val front = measurables.last().measure(Constraints.fixedWidth(width))
        val own = measurables.map { if (it == measurables.last()) front.height else it.minIntrinsicHeight(width) }
        val heights = own.map { lerp(front.height, it, progress) }
        val placeables =
            measurables.dropLast(1).mapIndexed { index, measurable ->
                measurable.measure(Constraints.fixed(width, heights[index]))
            } + front
        val gap = ToastTokens.gap.roundToPx()
        val peek = ToastTokens.peek.roundToPx()
        val stacked = front.height + peek * minOf(count - 1, ToastTokens.PEEK_LIMIT)
        val listed = own.sum() + gap * (count - 1)
        val height = lerp(stacked, listed, progress)
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val depth = count - 1 - index
                val collapsed = height - front.height - peek * minOf(depth, ToastTokens.PEEK_LIMIT)
                val inFront = heights.drop(index + 1).sum() + gap * depth
                val expanded = height - inFront - placeable.height
                placeable.place(0, lerp(collapsed, expanded, progress))
            }
        }
    }
}

/** One toast at [depth] places behind the front, drawn for the stacked or fanned-out group. */
@Composable
private fun ToastItem(
    state: ToastState,
    entry: ToastEntry,
    depth: Int,
    fan: () -> Float,
) {
    val expanded = state.expanded
    val reachable = expanded || depth == 0
    // The group places a stacked toast at its new depth at once; this eases it there, so a toast that
    // a newer one pushes back rises smoothly instead of jumping.
    val shownDepth by animateFloatAsState(
        minOf(depth, ToastTokens.PEEK_LIMIT).toFloat(),
        motionTween(MotionSpeed.Normal),
        label = "Toast depth",
    )
    val peek = with(LocalDensity.current) { ToastTokens.peek.toPx() }
    val scale by animateFloatAsState(
        if (expanded) 1f else 1f - ToastTokens.PEEK_SCALE_STEP * depth,
        motionTween(MotionSpeed.Normal),
        label = "Toast scale",
    )
    val peeking by animateFloatAsState(
        if (expanded || depth <= ToastTokens.PEEK_LIMIT) 1f else 0f,
        motionTween(MotionSpeed.Normal),
        label = "Toast peek",
    )
    val presence = remember { Animatable(0f) }
    val sink = ActionBarTokens.barInset
    val enter = motionTween<Float>(MotionSpeed.Normal, MotionCurve.Enter)
    val exit = motionTween<Float>(MotionSpeed.Normal, MotionCurve.Exit)
    LaunchedEffect(entry.leaving) {
        if (entry.leaving) {
            presence.animateTo(0f, exit)
            state.remove(entry, entry.acted)
        } else {
            presence.animateTo(1f, enter)
        }
    }
    SwipeToDismiss(
        enabled = reachable,
        onHeld = { entry.held = it },
        onDismiss = { state.remove(entry) },
        modifier =
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    // Scaled about its center, so it moves up by the height it loses to keep its top edge where
                    // the group placed it.
                    // Arriving and leaving, it rises from or sinks below the group's bottom, behind any bar.
                    val away = 1f - presence.value
                    val shown = scale * (1f - (1f - ToastTokens.AWAY_SCALE) * away)
                    val settling = peek * (minOf(depth, ToastTokens.PEEK_LIMIT) - shownDepth) * (1f - fan())
                    translationY = settling - size.height * (1f - scale) / 2f + away * (size.height + sink.toPx())
                    scaleX = shown
                    scaleY = shown
                    alpha = peeking * presence.value
                }.liveToast(reachable) { state.remove(entry) }
                .clickable(
                    remember { MutableInteractionSource() },
                    indication = null,
                    enabled = state.entries.size > 1,
                    onClickLabel = if (expanded) "Stack toasts" else "Show all toasts",
                ) { state.expanded = !expanded },
    ) { lifted -> ToastContent(state, entry, reachable, lifted) }
}

/** The toast itself. A toast behind the front one only fans the group out; its action stays out of reach. */
@Composable
private fun ToastContent(
    state: ToastState,
    entry: ToastEntry,
    reachable: Boolean,
    lifted: Boolean,
) {
    // Running the action is the toast's purpose, so it leaves with it.
    val action =
        entry.message.action?.let { shown ->
            ToastAction(shown.label) {
                shown.onAction()
                state.leave(entry, acted = true)
            }
        }
    Box {
        Toast(entry.message.text, action, entry.remaining, lifted = lifted)
        if (!reachable) {
            Box(
                Modifier.matchParentSize().clickable(interactionSource = null, indication = null) {
                    state.expanded =
                        true
                },
            )
        }
    }
}

/** Announces the toast to screen readers when it appears or changes, and offers the swipe as an action. */
private fun Modifier.liveToast(
    reachable: Boolean,
    onDismiss: () -> Unit,
): Modifier {
    if (!reachable) return clearAndSetSemantics {}
    return semantics {
        liveRegion = LiveRegionMode.Polite
        customActions =
            listOf(
                CustomAccessibilityAction("Dismiss") {
                    onDismiss()
                    true
                },
            )
    }
}

/**
 * Advances every timer once per frame while a toast is on screen. Screen reader users get the
 * longer time Android recommends for content with controls.
 */
@Composable
private fun RunTimers(state: ToastState) {
    val accessibility by rememberUpdatedState(LocalAccessibilityManager.current)
    LaunchedEffect(state) {
        snapshotFlow { state.entries.any { !it.leaving } }.collectLatest { active ->
            if (!active) return@collectLatest
            var last = withFrameMillis { it }
            while (true) {
                val now = withFrameMillis { it }
                state.tick(now - last) { accessibility.timeout(it) }
                last = now
            }
        }
    }
}

private fun AccessibilityManager?.timeout(message: ToastMessage): Long =
    this?.calculateRecommendedTimeoutMillis(
        baseTimeout(message),
        containsIcons = message.action != null,
        containsText = true,
        containsControls = message.action != null,
    ) ?: baseTimeout(message)

/** Fades whatever moves into the outer bar inset at either side, such as a toast swiped away. */
private fun Modifier.screenEdgeFade(): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }.drawWithContent {
        drawContent()
        val fade = ActionBarTokens.barInset.toPx()
        drawEdgeFade(1f, fade, atEnd = false)
        drawEdgeFade(1f, fade, atEnd = true)
    }
