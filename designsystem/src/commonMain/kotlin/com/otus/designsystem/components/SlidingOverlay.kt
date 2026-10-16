package com.otus.designsystem.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.theme.LocalMotionScale

/**
 * Shows [base] and, while [item] is set, a page for it on top, such as an open message over its
 * list. The page slides in from the end edge and pushes [base] out, as [SlidingPages] does; when
 * [item] clears, the page keeps its last item while it slides back out. [base] stays composed
 * throughout, so it keeps its scroll and state.
 */
@Composable
public fun <T : Any> SlidingOverlay(
    item: T?,
    modifier: Modifier = Modifier,
    base: @Composable () -> Unit,
    overlay: @Composable (T) -> Unit,
) {
    val motionScale = LocalMotionScale.current
    val sign = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
    val last = remember { LastItem<T>() }
    if (item != null) last.value = item
    val transition = updateTransition(item != null, label = "Overlay")
    val progress =
        transition.animateFloat(
            transitionSpec = { pageSlide(back = !targetState, motionScale) },
            label = "Overlay slide",
        ) { if (it) 1f else 0f }
    Box(modifier) {
        Box(Modifier.graphicsLayer { translationX = -progress.value * size.width * sign }) { base() }
        val shown = last.value
        if (shown != null && (transition.currentState || transition.targetState)) {
            Box(Modifier.graphicsLayer { translationX = (1f - progress.value) * size.width * sign }) { overlay(shown) }
        }
    }
}

/** The overlay's last item, kept outside snapshot state since it only changes along with [SlidingOverlay]'s own. */
private class LastItem<T> {
    var value: T? = null
}
