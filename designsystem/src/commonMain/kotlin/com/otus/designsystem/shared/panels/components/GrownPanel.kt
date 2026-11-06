package com.otus.designsystem.shared.panels.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.shared.pages.elements.hiddenWhen
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.layout.QuickViewTokens

// The steps a page's control panel grows through: its usual height, a quick view, and a last
// stop, such as every filter or every detail.

/** A grown panel's last stop, such as every filter. */
public const val GROWN_LAST_STOP: Int = 2

/** Where the last stop takes over from the quick view, halfway to it. */
public const val GROWN_LAST_STOP_FROM: Float = 1.5f

/** Where the quick view starts to show, as the panel leaves its usual height. */
private const val QUICK_FROM = 0.3f

/** A quick view's height with [modules] picked: the modules, its last row, and the grab handle. */
public fun quickViewHeight(modules: Int): Dp =
    QuickViewTokens.moduleGap +
        (QuickViewTokens.moduleHeight + QuickViewTokens.moduleGap) * modules +
        QuickViewTokens.jumpHeight +
        PanelTokens.handlePadding * 2 +
        PanelTokens.handleHeight

/** How much a grown panel's quick view shows at [position]: in toward its stop, out toward the next. */
internal fun quickViewAlpha(position: Float): Float =
    when {
        position <= QUICK_FROM -> 0f
        position <= 1f -> (position - QUICK_FROM) / (1f - QUICK_FROM)
        else -> (1f - (position - 1f) / (GROWN_LAST_STOP_FROM - 1f)).coerceAtLeast(0f)
    }

/** How much a grown panel's last stop shows at [position]. */
internal fun lastStopAlpha(position: Float): Float =
    ((position - GROWN_LAST_STOP_FROM) / (GROWN_LAST_STOP - GROWN_LAST_STOP_FROM)).coerceIn(0f, 1f)

/** The most a panel grows: the window's height less the room [above] and [below] it. */
@Composable
public fun grownPanelHeight(
    above: Dp,
    below: Dp,
): Dp {
    val window =
        with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.height
                .toDp()
        }
    return window - above - below
}

/** Whether any of [expansion]'s last stop shows. */
@Composable
public fun rememberLastStopShows(expansion: PanelExpansion): Boolean {
    val shows by remember(expansion) { derivedStateOf { lastStopAlpha(expansion.position) > 0f } }
    return shows
}

/**
 * Whether a layer shows: while [open], and on until [expansion] stops moving after, so a drag that
 * started in it keeps it under the finger and ends where the panel settles.
 */
@Composable
public fun rememberShownWhileMoving(
    expansion: PanelExpansion,
    open: Boolean,
): Boolean {
    var shown by remember { mutableStateOf(open) }
    if (open) {
        shown = true
    } else if (!expansion.moving) {
        shown = false
    }
    return shown
}

/** Whether a layer shows: while [open], and on until [expansion] has closed after. */
@Composable
public fun rememberShownUntilClosed(
    expansion: PanelExpansion,
    open: Boolean,
): Boolean {
    val expanded by remember(expansion) { derivedStateOf { expansion.expanded } }
    var shown by remember { mutableStateOf(open) }
    if (open) {
        shown = true
    } else if (!expanded) {
        shown = false
    }
    return shown
}

/**
 * A grown panel's room: the [quick] view around the first stop, and the [lastStop] toward the last,
 * each fading with how far [expansion] is open and leaving the accessibility tree once faded out.
 * The quick view stays hidden while the panel skips past it. The last stop is composed only while
 * it is [lastStopOpen], and until the panel stops moving after, so its faded layer takes no taps.
 */
@Composable
public fun GrownPanelLayers(
    expansion: PanelExpansion,
    lastStopOpen: Boolean,
    quick: @Composable () -> Unit,
    lastStop: @Composable () -> Unit,
) {
    val position = { expansion.position }
    val quickHidden by remember(expansion) {
        derivedStateOf { expansion.skipping || quickViewAlpha(position()) == 0f }
    }
    val lastHidden by remember(expansion) { derivedStateOf { lastStopAlpha(position()) == 0f } }
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .graphicsLayer { alpha = if (expansion.skipping) 0f else quickViewAlpha(position()) }
                .hiddenWhen(quickHidden),
        ) { quick() }
        if (rememberShownWhileMoving(expansion, lastStopOpen)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = lastStopAlpha(position()) }
                    .hiddenWhen(lastHidden),
            ) { lastStop() }
        }
    }
}
