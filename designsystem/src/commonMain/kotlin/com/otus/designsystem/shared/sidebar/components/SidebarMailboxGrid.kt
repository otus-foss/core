package com.otus.designsystem.shared.sidebar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.theme.tokens.layout.SidebarTokens

/**
 * [items] in three equal columns, each drawn by [tile], such as a
 * [com.otus.designsystem.shared.sidebar.elements.SidebarMailboxTile]. A short last row keeps its tiles at the
 * start, the same width as the rest. Gaps separate the tiles from each other and from the row
 * above the grid.
 */
@Composable
public fun <T> SidebarMailboxGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    tile: @Composable (T) -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            // Inset like a SidebarDivider, so the grid's edges line up with the hairlines.
            .padding(horizontal = SidebarTokens.entryPadding)
            .padding(top = SidebarTokens.tileGap)
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(SidebarTokens.tileGap),
    ) {
        items.chunked(SidebarTokens.TILE_COLUMNS).forEach { row ->
            MailboxTileRow(row, tile)
        }
    }
}

@Composable
private fun <T> MailboxTileRow(
    items: List<T>,
    tile: @Composable (T) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SidebarTokens.tileGap)) {
        items.forEach { item -> Box(Modifier.weight(1f)) { tile(item) } }
        repeat(SidebarTokens.TILE_COLUMNS - items.size) { Spacer(Modifier.weight(1f)) }
    }
}
