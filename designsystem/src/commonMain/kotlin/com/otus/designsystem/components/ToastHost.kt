package com.otus.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.otus.designsystem.elements.Toast
import com.otus.designsystem.elements.ToastAction
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.MotionCurve
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.ToastTokens
import com.otus.designsystem.theme.motionTween
import kotlinx.coroutines.flow.collectLatest

/**
 * Shows the toasts in [state] as a stack: the newest in front and up to two older ones peeking
 * out behind it, each with its own timer. Tapping the stack fans it out into a list and pauses
 * every timer; tapping again or system back stacks it again. Any toast in front, or any toast in
 * the list, can be swiped away in either direction; dismissing one with an action leaves the action
 * done. A toast whose time is up or whose action ran sinks, shrinks, and fades on the exit curve,
 * behind the bar when there is one; a new one rises the same way on the enter curve. The group
 * is as wide as its widest toast. Align it to the bottom of the window with [modifier]; it rises
 * above any bar in [LocalBottomClearance].
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
    val count = state.entries.size
    val height by animateDpAsState(
        if (count == 0) {
            0.dp
        } else if (state.expanded) {
            ToastTokens.height * count + ToastTokens.gap * (count - 1)
        } else {
            ToastTokens.height + ToastTokens.peek * minOf(count - 1, ToastTokens.PEEK_LIMIT)
        },
        motionTween(MotionSpeed.Normal),
        label = "Toast group height",
    )
    Box(modifier.aboveBottomBars().width(IntrinsicSize.Max).height(height)) {
        state.entries.forEachIndexed { index, entry ->
            key(entry.id) {
                ToastItem(state, entry, depth = count - 1 - index, Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}

/** One toast at [depth] places behind the front, positioned for the stacked or fanned-out group. */
@Composable
private fun ToastItem(
    state: ToastState,
    entry: ToastEntry,
    depth: Int,
    modifier: Modifier,
) {
    val density = LocalDensity.current
    val expanded = state.expanded
    val reachable = expanded || depth == 0
    val rise = if (expanded) (ToastTokens.height + ToastTokens.gap) * depth else ToastTokens.peek * depth
    val lift by animateFloatAsState(
        with(density) { rise.toPx() },
        motionTween(MotionSpeed.Normal),
        label = "Toast lift",
    )
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
    ToastSwipe(
        enabled = reachable,
        onHeld = { entry.held = it },
        onDismiss = { state.remove(entry) },
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    // Scaled about its center, so it moves up by the height it loses to keep its top edge at the peek.
                    // Arriving and leaving, it rises from or sinks below the group's bottom, behind any bar.
                    val away = 1f - presence.value
                    val shown = scale * (1f - (1f - ToastTokens.AWAY_SCALE) * away)
                    translationY = -lift - size.height * (1f - scale) / 2f + away * (size.height + sink.toPx())
                    scaleX = shown
                    scaleY = shown
                    alpha = peeking * presence.value
                }.then(if (reachable) Modifier.liveToast { state.remove(entry) } else Modifier.clearAndSetSemantics {})
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
        if (!reachable) Box(Modifier.matchParentSize().clickable(null, indication = null) { state.expanded = true })
    }
}

/** Announces the toast to screen readers when it appears or changes, and offers the swipe as an action. */
private fun Modifier.liveToast(onDismiss: () -> Unit): Modifier =
    semantics {
        liveRegion = LiveRegionMode.Polite
        customActions =
            listOf(
                CustomAccessibilityAction("Dismiss") {
                    onDismiss()
                    true
                },
            )
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
