package com.otus.designsystem.shared.panels.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
internal fun rememberShownWhileMoving(
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
 * [shared], such as a message's details, sits above both and stays still while one fades into the
 * other, so it never redraws between the stops; it scrolls when the stop is too short for it.
 * The quick view stays hidden while the panel skips past it. The last stop is composed only while
 * it is [lastStopOpen], and until the panel stops moving after, so its faded layer takes no taps.
 */
@Composable
public fun GrownPanelLayers(
    expansion: PanelExpansion,
    lastStopOpen: Boolean,
    quick: @Composable () -> Unit,
    lastStop: @Composable () -> Unit,
    shared: (@Composable () -> Unit)? = null,
) {
    val position = { expansion.position }
    val sharedHidden by remember(expansion) { derivedStateOf { sharedAlpha(position(), expansion.skipping) == 0f } }
    if (shared == null) {
        Layers(expansion, lastStopOpen, quick, lastStop, fill = true)
        return
    }
    // The layers keep their own height and the shared part takes the rest, scrolling when it is
    // taller, so what sits under it stays in reach.
    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .graphicsLayer { alpha = sharedAlpha(position(), expansion.skipping) }
                .hiddenWhen(sharedHidden),
        ) { shared() }
        Layers(expansion, lastStopOpen, quick, lastStop, fill = false)
    }
}

@Composable
private fun Layers(
    expansion: PanelExpansion,
    lastStopOpen: Boolean,
    quick: @Composable () -> Unit,
    lastStop: @Composable () -> Unit,
    fill: Boolean,
) {
    val position = { expansion.position }
    val size = if (fill) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
    val quickHidden by remember(expansion) {
        derivedStateOf { expansion.skipping || quickViewAlpha(position()) == 0f }
    }
    val lastHidden by remember(expansion) { derivedStateOf { lastStopAlpha(position()) == 0f } }
    Box(size) {
        Box(
            Modifier
                .graphicsLayer { alpha = if (expansion.skipping) 0f else quickViewAlpha(position()) }
                .hiddenWhen(quickHidden),
        ) { quick() }
        if (rememberShownWhileMoving(expansion, lastStopOpen)) {
            Box(
                size
                    .graphicsLayer { alpha = lastStopAlpha(position()) }
                    .hiddenWhen(lastHidden),
            ) { lastStop() }
        }
    }
}
