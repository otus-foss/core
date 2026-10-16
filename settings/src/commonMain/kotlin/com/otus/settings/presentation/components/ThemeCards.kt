package com.otus.settings.presentation.components

import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.components.ThemeCard
import com.otus.designsystem.theme.ThemeChoice
import com.otus.settings.presentation.ThemeOption
import com.otus.settings.presentation.options

/**
 * Every theme, built-in and custom, as a card in two columns, each sketched in its own colors. A
 * long press or the selected card's more button runs [onActions] for that theme.
 */
@Composable
internal fun ThemeCards(
    choice: ThemeChoice,
    onSelect: (ThemeChoice) -> Unit,
    onActions: (ThemeOption) -> Unit,
) {
    PairedGrid(choice.options, Modifier.selectableGroup()) { option, modifier ->
        ThemeCard(
            title = option.title,
            description = option.description,
            sketch = option.choice.palette,
            selected = option.selected,
            onClick = { onSelect(option.choice) },
            modifier = modifier,
            onActions = { onActions(option) },
        )
    }
}
