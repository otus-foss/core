package com.otus.designsystem.shared.sidebar.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import com.otus.designsystem.shared.sidebar.elements.SidebarTileSize
import com.otus.designsystem.theme.tokens.layout.SidebarTokens

/**
 * [items] in three equal columns, each drawn by [tile], such as a
 * [com.otus.designsystem.shared.sidebar.elements.SidebarMailboxTile], spanning as many columns as
 * [sizeOf] says. Tiles fill each row in order, and one that does not fit the rest of a row starts
 * the next, leaving a gap. Each row is as tall as its tallest tile needs.
 */
@Composable
public fun <T> SidebarMailboxGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    sizeOf: (T) -> SidebarTileSize = { SidebarTileSize.Small },
    tile: @Composable (T) -> Unit,
) {
    val slots = tileSlots(items.map(sizeOf))
    Layout(
        content = { items.forEach { item -> Box(propagateMinConstraints = true) { tile(item) } } },
        modifier =
            modifier
                .fillMaxWidth()
                // Inset like a SidebarDivider, so the grid's edges line up with the hairlines.
                .padding(horizontal = SidebarTokens.entryPadding)
                .selectableGroup(),
    ) { measurables, constraints ->
        val gap = SidebarTokens.tileGap.roundToPx()
        val widths = slots.map { tileWidth(it.size.columns, constraints.maxWidth, gap) }
        val bounds =
            tileBounds(slots, constraints.maxWidth, gap) { row ->
                row.maxOf { measurables[it].minIntrinsicHeight(widths[it]) }
            }
        val placeables =
            measurables.zip(bounds) { measurable, bound ->
                measurable.measure(Constraints.fixed(bound.width, bound.height))
            }
        layout(constraints.maxWidth, bounds.maxOfOrNull { it.bottom } ?: 0) {
            placeables.zip(bounds) { placeable, bound -> placeable.place(bound.left, bound.top) }
        }
    }
}
