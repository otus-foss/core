package com.otusfoss.core.settings.presentation.components

import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import com.otusfoss.core.designsystem.shared.settings.components.ThemeCard
import com.otusfoss.core.designsystem.theme.colors.ThemeChoice
import com.otusfoss.core.settings.presentation.ThemeOption
import com.otusfoss.core.settings.presentation.options

/**
 * Every theme, built-in and custom, as a card in two columns, each sketched in its own colors. A
 * long press or the selected card's more button opens that theme's [actions].
 */
@Composable
internal fun ThemeCards(
    choice: ThemeChoice,
    onSelect: (ThemeChoice) -> Unit,
    actions: (ThemeOption) -> List<UiAction>,
) {
    CardGrid(choice.options, Modifier.selectableGroup()) { option, modifier ->
        ThemeCard(
            title = option.title,
            sketch = option.choice.palette,
            selected = option.selected,
            onClick = { onSelect(option.choice) },
            modifier = modifier,
            actions = actions(option),
        )
    }
}
