package com.otus.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.theme.SidebarTokens

/**
 * [items] in three equal columns, each drawn by [tile], such as a
 * [com.otus.designsystem.elements.SidebarMailboxTile]. A short last row keeps its tiles at the
 * start, the same width as the rest.
 */
@Composable
public fun <T> SidebarMailboxGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    tile: @Composable (T) -> Unit,
) {
    Column(modifier.fillMaxWidth().selectableGroup()) {
        items.chunked(SidebarTokens.TILE_COLUMNS).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { item -> Box(Modifier.weight(1f)) { tile(item) } }
                repeat(SidebarTokens.TILE_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
