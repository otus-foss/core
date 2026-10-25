package com.otus.settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.components.ColorSheet
import com.otus.designsystem.components.InfoText
import com.otus.designsystem.components.InfoTip
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.OtusPalette
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.ThemeChoice
import com.otus.designsystem.theme.ThemeTokens
import com.otus.settings.presentation.options

/**
 * Picks any accent, starting from [choice]'s. Each theme previews the shades Otus makes from it;
 * Done runs [onPick] with the color.
 */
@Composable
internal fun CustomAccentSheet(
    choice: ThemeChoice,
    onPick: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    ColorSheet("Custom color", choice.accent, onPick, onDismiss) { color ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "How it looks in each theme",
                Modifier.padding(horizontal = PanelTokens.iconGap),
                color = OtusColors.text2,
            )
            InfoTip(AccentInfo)
        }
        PairedGrid(choice.options) { option, modifier ->
            ThemePreview(option.title, option.choice.withAccent(color).palette, modifier)
        }
    }
}

/** A pill and accent text in [palette]'s colors, under the theme's [title]. */
@Composable
private fun ThemePreview(
    title: String,
    palette: OtusPalette,
    modifier: Modifier,
) {
    val shape = RoundedCornerShape(ThemeTokens.customPreviewRadius)
    Column(
        modifier
            .background(palette.bgRegular, shape)
            .border(PanelTokens.borderWidth, palette.strokeBgDark, shape)
            .padding(ThemeTokens.customPreviewPadding),
        verticalArrangement = Arrangement.spacedBy(ThemeTokens.cardGap),
    ) {
        Text(
            title,
            color = palette.text2,
            fontSize = ThemeTokens.roleCaptionSize,
            lineHeight = ThemeTokens.roleCaptionLineHeight,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(PanelTokens.chipGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .height(ThemeTokens.customPillHeight)
                    .background(palette.accentDark, CircleShape)
                    .border(PanelTokens.borderWidth, palette.accentLight, CircleShape)
                    .padding(horizontal = ThemeTokens.customPillPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Inbox",
                    color = palette.text1,
                    fontSize = ThemeTokens.roleCaptionSize,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text("Done", color = palette.accentLight, fontWeight = FontWeight.SemiBold)
        }
    }
}

private val AccentInfo =
    InfoText(
        "Custom color",
        "You pick one color. Otus makes a fill, an outline and a text shade for each theme, and keeps " +
            "text readable on every one.",
    )
