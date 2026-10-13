package com.otus.settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.elements.AddSwatch
import com.otus.designsystem.elements.ColorSwatch
import com.otus.designsystem.theme.AccentPreset
import com.otus.designsystem.theme.AccentShades
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.ThemeChoice
import com.otus.designsystem.theme.ThemeTokens
import com.otus.designsystem.theme.toHex
import com.otus.settings.presentation.title

/**
 * The accent swatches, a custom color, and the three shades Otus makes from the picked one in
 * [choice]'s theme, with a note on how they were made.
 */
@Composable
internal fun AccentPicker(
    choice: ThemeChoice,
    onSelect: (Color) -> Unit,
    onCustom: () -> Unit,
) {
    val shape = RoundedCornerShape(SheetTokens.radius)
    Column(
        Modifier
            .fillMaxWidth()
            .background(OtusColors.bgDark, shape)
            .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape)
            .padding(ThemeTokens.accentCardPadding),
        verticalArrangement = Arrangement.spacedBy(ThemeTokens.accentCardGap),
    ) {
        FlowRow(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(ThemeTokens.swatchGap)) {
            AccentPreset.entries.forEach { preset ->
                ColorSwatch(preset.color, preset.title, preset.color == choice.accent, { onSelect(preset.color) })
            }
            if (AccentPreset.of(choice.accent) == null) {
                ColorSwatch(choice.accent, "Custom color ${choice.accent.toHex()}", selected = true, onClick = onCustom)
            }
            AddSwatch("Custom color", onCustom)
        }
        AccentRoles(choice.shades)
        Text(
            accentNote(choice),
            Modifier.padding(horizontal = PanelTokens.iconGap),
            color = OtusColors.text2,
        )
    }
}

/** How the three shades look: a selected fill, a primary outline, and accent text. */
@Composable
private fun AccentRoles(shades: AccentShades) {
    Row(horizontalArrangement = Arrangement.spacedBy(ThemeTokens.cardGap)) {
        AccentRole("Fill · ${shades.fill.toHex()}") {
            RolePill("Selected", Modifier.background(shades.fill, CircleShape))
        }
        AccentRole("Outline · ${shades.regular.toHex()}") {
            RolePill("Primary", Modifier.border(PanelTokens.borderWidth, shades.regular, CircleShape))
        }
        AccentRole("Text · ${shades.text.toHex()}") {
            Box(Modifier.fillMaxWidth().height(ThemeTokens.rolePillHeight), contentAlignment = Alignment.Center) {
                Text("Done", color = shades.text, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RowScope.AccentRole(
    caption: String,
    sample: @Composable () -> Unit,
) {
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PanelTokens.chipGap)) {
        sample()
        Text(
            caption,
            Modifier.padding(horizontal = PanelTokens.iconGap),
            color = OtusColors.text2,
            fontSize = ThemeTokens.roleCaptionSize,
            lineHeight = ThemeTokens.roleCaptionLineHeight,
        )
    }
}

@Composable
private fun RolePill(
    label: String,
    modifier: Modifier,
) {
    Box(modifier.fillMaxWidth().height(ThemeTokens.rolePillHeight), contentAlignment = Alignment.Center) {
        Text(label, fontSize = ThemeTokens.roleLabelSize)
    }
}

private fun accentNote(choice: ThemeChoice): String {
    val name = AccentPreset.of(choice.accent)?.title?.lowercase() ?: "your color"
    return if (choice.shades.adjusted) {
        "Otus made the other two shades from $name and adjusted the text shade so it stays readable in " +
            "${choice.base.title}."
    } else {
        "Otus made the other two shades from $name. Every screen uses them."
    }
}
