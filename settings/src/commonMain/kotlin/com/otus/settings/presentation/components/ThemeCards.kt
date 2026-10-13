package com.otus.settings.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.components.ThemeCard
import com.otus.designsystem.theme.ThemeBase
import com.otus.designsystem.theme.ThemeChoice
import com.otus.designsystem.theme.ThemeTokens
import com.otus.settings.presentation.description
import com.otus.settings.presentation.title

/** Every theme as a card in two columns, each sketched with [choice]'s accent. */
@Composable
internal fun ThemeCards(
    choice: ThemeChoice,
    onSelect: (ThemeBase) -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(ThemeTokens.cardGap),
    ) {
        ThemeBase.entries.chunked(COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(ThemeTokens.cardGap)) {
                row.forEach { base ->
                    ThemeCard(
                        title = base.title,
                        description = base.description,
                        sketch = choice.copy(base = base).palette,
                        selected = base == choice.base,
                        onClick = { onSelect(base) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private const val COLUMNS = 2
