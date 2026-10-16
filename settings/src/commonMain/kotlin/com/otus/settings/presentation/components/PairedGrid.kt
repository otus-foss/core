package com.otus.settings.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.theme.ThemeTokens

/** [items] in two equal columns; a last item on its own keeps the width of one column. */
@Composable
internal fun <T> PairedGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    item: @Composable (T, Modifier) -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ThemeTokens.cardGap)) {
        items.chunked(COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(ThemeTokens.cardGap)) {
                row.forEach { item(it, Modifier.weight(1f)) }
                repeat(COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private const val COLUMNS = 2
