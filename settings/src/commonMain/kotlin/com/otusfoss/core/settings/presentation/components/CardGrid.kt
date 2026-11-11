package com.otusfoss.core.settings.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.theme.tokens.settings.ThemeTokens

/** [items] in equal [columns]; a short last row keeps each item the width of one column. */
@Composable
internal fun <T> CardGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    item: @Composable (T, Modifier) -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ThemeTokens.cardGap)) {
        items.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(ThemeTokens.cardGap)) {
                row.forEach { item(it, Modifier.weight(1f)) }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
