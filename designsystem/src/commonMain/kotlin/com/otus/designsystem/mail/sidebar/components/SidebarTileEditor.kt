package com.otus.designsystem.mail.sidebar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.round
import com.otus.designsystem.mail.sidebar.elements.SidebarTileSize
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.layout.CornerTokens
import com.otus.designsystem.theme.tokens.layout.SidebarTokens

/**
 * Arranges the tiles of a [SidebarMailboxGrid] the way Control Center does, on a panel with the
 * sidebar's surface and width, so it shows the grid as the sidebar will. Long-press a tile and
 * drag it to move it; the other tiles step aside. Drag the arc on a tile's bottom-end corner
 * sideways to change its [SidebarTileSize]. The minus on its top-start corner removes it.
 * [tileOf] describes each item. [onMove] reports a move by index, and [onResize] and [onRemove]
 * report the item; the caller owns the items. [tile] draws an item at its size, such as a
 * [com.otus.designsystem.mail.sidebar.elements.SidebarMailboxTile] without a click.
 */
@Composable
public fun <T> SidebarTileEditor(
    items: List<T>,
    tileOf: (T) -> EditableTile,
    onMove: (from: Int, to: Int) -> Unit,
    onResize: (item: T, size: SidebarTileSize) -> Unit,
    onRemove: (item: T) -> Unit,
    modifier: Modifier = Modifier,
    tile: @Composable (T) -> Unit,
) {
    val tiles = items.map(tileOf)
    val keys = tiles.map { it.key }
    val sizes = tiles.associate { it.key to it.size }
    val drag = remember { TileDrag() }
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
    SideEffect { drag.sync(keys) }
    BoxWithConstraints(modifier.editorPanel()) {
        val grid =
            with(density) {
                TileGrid(constraints.maxWidth, SidebarTokens.tileGap.roundToPx(), SidebarTokens.tileHeight.roundToPx())
            }
        val place = { order: List<Any> -> grid.bounds(order.map(sizes::getValue)) }
        val order = drag.shown(keys)
        val bounds = order.zip(place(order)).toMap()
        Box(Modifier.fillMaxWidth().height(with(density) { (bounds.values.maxOfOrNull { it.bottom } ?: 0).toDp() })) {
            items.zip(tiles).forEachIndexed { index, (item, described) ->
                key(described.key) {
                    val moves =
                        TileMoves(
                            onLift = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                drag.start(described.key, bounds.getValue(described.key).topLeft)
                            },
                            onDrag = { delta ->
                                if (drag.moveBy(Offset(delta.x * direction, delta.y), place)) {
                                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                }
                            },
                            onDrop = { drag.drop()?.let { (from, to) -> onMove(from, to) } },
                            onCancel = drag::cancel,
                            onEarlier = { onMove(index, index - 1) }.takeIf { index > 0 },
                            onLater = { onMove(index, index + 1) }.takeIf { index < items.lastIndex },
                        )
                    EditorTile(
                        bounds.getValue(described.key),
                        drag.position.round().takeIf { drag.key == described.key },
                        TileEdits(
                            described.label,
                            described.size,
                            grid::sizeAt,
                            onResize = { onResize(item, it) },
                            onRemove = { onRemove(item) },
                            moves,
                        ),
                    ) { tile(item) }
                }
            }
        }
    }
}

/** The sidebar's width and surface, with room around the tiles for the minus badges on their corners. */
private fun Modifier.editorPanel(): Modifier =
    fillMaxWidth()
        .wrapContentWidth()
        .widthIn(max = SidebarTokens.width)
        .fillMaxWidth()
        .background(OtusColors.bgDark, SmoothCornerShape(CornerTokens.panel))
        .padding(SidebarTokens.entryPadding)
