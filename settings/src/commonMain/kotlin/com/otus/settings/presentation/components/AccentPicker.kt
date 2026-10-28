package com.otus.settings.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.otus.designsystem.shared.settings.elements.AddSwatch
import com.otus.designsystem.shared.settings.elements.ColorSwatch
import com.otus.designsystem.theme.colors.AccentPreset
import com.otus.designsystem.theme.colors.toHex
import com.otus.designsystem.theme.tokens.settings.ThemeTokens
import com.otus.settings.presentation.title

/** The accent swatches, [accent] when it is a custom color, and a swatch to pick a custom one. */
@Composable
internal fun AccentPicker(
    accent: Color,
    onSelect: (Color) -> Unit,
    onCustom: () -> Unit,
) {
    FlowRow(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(ThemeTokens.swatchGap)) {
        AccentPreset.entries.forEach { preset ->
            ColorSwatch(preset.color, preset.title, preset.color == accent, { onSelect(preset.color) })
        }
        if (AccentPreset.of(accent) == null) {
            ColorSwatch(accent, "Custom color ${accent.toHex()}", selected = true, onClick = onCustom)
        }
        AddSwatch("Custom color", onCustom)
    }
}
