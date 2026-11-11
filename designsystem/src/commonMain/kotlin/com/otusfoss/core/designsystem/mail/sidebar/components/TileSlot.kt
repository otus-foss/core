package com.otusfoss.core.designsystem.mail.sidebar.components

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.otusfoss.core.designsystem.mail.sidebar.elements.SidebarTileSize
import com.otusfoss.core.designsystem.theme.tokens.layout.SidebarTokens
import kotlin.math.abs
import kotlin.math.roundToInt

/** Where a tile sits in a [SidebarMailboxGrid]: its [row], first [column], and [size]. */
internal data class TileSlot(
    val row: Int,
    val column: Int,
    val size: SidebarTileSize,
) {
    val end: Int
        get() = column + size.columns
}

/** Places tiles of [sizes] in order, by row; a tile that does not fit the rest of a row starts the next. */
internal fun tileSlots(sizes: List<SidebarTileSize>): List<TileSlot> =
    sizes.fold(emptyList()) { slots, size ->
        val last = slots.lastOrNull()
        slots +
            when {
                last == null -> TileSlot(0, 0, size)
                last.end + size.columns <= SidebarTokens.TILE_COLUMNS -> TileSlot(last.row, last.end, size)
                else -> TileSlot(last.row + 1, 0, size)
            }
    }

/**
 * The bounds of each of [slots] in a grid [width] pixels wide, with [gap] pixels between tiles.
 * [rowHeight] gets the indices of one row's slots and returns that row's height.
 */
internal fun tileBounds(
    slots: List<TileSlot>,
    width: Int,
    gap: Int,
    rowHeight: (List<Int>) -> Int,
): List<IntRect> {
    val cell = cellWidth(width, gap)
    // Slots come in row order, so the groups do too.
    val heights =
        slots.indices
            .groupBy { slots[it].row }
            .values
            .map(rowHeight)
    val tops = heights.runningFold(0) { top, height -> top + height + gap }
    return slots.map { slot ->
        IntRect(
            IntOffset((slot.column * (cell + gap)).roundToInt(), tops[slot.row]),
            IntSize(tileWidth(slot.size.columns, width, gap), heights[slot.row]),
        )
    }
}

/** The width of a tile [columns] wide, with the gaps between its columns, in a grid [width] pixels wide. */
internal fun tileWidth(
    columns: Int,
    width: Int,
    gap: Int,
): Int = (cellWidth(width, gap) * columns + gap * (columns - 1)).roundToInt()

private fun cellWidth(
    width: Int,
    gap: Int,
): Float = (width - gap * (SidebarTokens.TILE_COLUMNS - 1)) / SidebarTokens.TILE_COLUMNS.toFloat()

/** A grid [width] pixels wide, with [gap] pixels between tiles and rows [height] pixels tall. */
internal class TileGrid(
    private val width: Int,
    private val gap: Int,
    private val height: Int,
) {
    fun bounds(sizes: List<SidebarTileSize>): List<IntRect> = tileBounds(tileSlots(sizes), width, gap) { height }

    /** The size whose width is nearest [reach] pixels. */
    fun sizeAt(reach: Float): SidebarTileSize =
        SidebarTileSize.entries.minBy { size -> abs(tileWidth(size.columns, width, gap) - reach) }
}
