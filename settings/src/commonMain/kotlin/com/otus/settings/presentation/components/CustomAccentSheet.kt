package com.otus.settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.components.SheetFrame
import com.otus.designsystem.elements.ColorSlider
import com.otus.designsystem.elements.InputField
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.ThemeBase
import com.otus.designsystem.theme.ThemeTokens
import com.otus.designsystem.theme.accentShades
import com.otus.designsystem.theme.hslColor
import com.otus.designsystem.theme.parseHexColor
import com.otus.designsystem.theme.toHex
import com.otus.designsystem.theme.toHsl
import com.otus.settings.presentation.title

/**
 * Picks any accent by hue and intensity, or by hex code, starting from [initial]. Each theme
 * previews the shades Otus makes from it; Done runs [onPick] with the color.
 */
@Composable
internal fun CustomAccentSheet(
    initial: Color,
    onPick: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    var color by remember { mutableStateOf(initial) }
    var hex by remember { mutableStateOf(initial.toHex()) }
    var hsl by remember { mutableStateOf(initial.toHsl()) }
    val slide = { hue: Float, saturation: Float ->
        hsl = hsl.copy(hue = hue, saturation = saturation, lightness = SLIDER_LIGHTNESS)
        color = hslColor(hue, saturation, SLIDER_LIGHTNESS)
        hex = color.toHex()
    }
    SheetFrame("Custom color", onDismiss = onDismiss, onDone = { onPick(color) }) {
        Column(
            Modifier.padding(horizontal = SheetTokens.inset).padding(bottom = SheetTokens.inset),
            verticalArrangement = Arrangement.spacedBy(SheetTokens.inset),
        ) {
            HexRow(color, hex) { text ->
                hex = text
                parseHexColor(text)?.let {
                    color = it
                    hsl = it.toHsl()
                }
            }
            ColorSlider(hsl.hue, { slide(it, hsl.saturation) }, hueTrack, "Hue", range = 0f..MAX_HUE)
            ColorSlider(
                hsl.saturation,
                { slide(hsl.hue, it) },
                Brush.horizontalGradient(
                    listOf(
                        hslColor(hsl.hue, MIN_INTENSITY, SLIDER_LIGHTNESS),
                        hslColor(hsl.hue, MAX_INTENSITY, SLIDER_LIGHTNESS),
                    ),
                ),
                "Intensity",
                range = MIN_INTENSITY..MAX_INTENSITY,
            )
            Text(
                "How it looks in each theme",
                Modifier.padding(horizontal = PanelTokens.iconGap),
                color = OtusColors.text2,
            )
            ThemeBase.entries.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(ThemeTokens.cardGap)) {
                    row.forEach { ThemePreview(it, color, Modifier.weight(1f)) }
                }
            }
            Text(
                "You pick one color. Otus makes a fill, an outline and a text shade for each theme, and keeps " +
                    "text readable on every one.",
                Modifier.padding(horizontal = PanelTokens.iconGap),
                color = OtusColors.text2,
            )
        }
    }
}

/** The picked [color] beside its [hex] code, which can be typed. */
@Composable
private fun HexRow(
    color: Color,
    hex: String,
    onHexChange: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(SheetTokens.inset), verticalAlignment = Alignment.Bottom) {
        Box(
            Modifier
                .size(ThemeTokens.customColorSize)
                .background(color, CircleShape)
                .border(PanelTokens.borderWidth, OtusColors.bgLight, CircleShape),
        )
        InputField("Hex", hex, onHexChange, Modifier.weight(1f), placeholder = "#4D6F92")
    }
}

/** A pill and accent text in [base]'s colors with [accent]'s shades. */
@Composable
private fun ThemePreview(
    base: ThemeBase,
    accent: Color,
    modifier: Modifier,
) {
    val shades = base.accentShades(accent)
    val shape = RoundedCornerShape(ThemeTokens.customPreviewRadius)
    Column(
        modifier
            .background(base.page, shape)
            .border(PanelTokens.borderWidth, base.stroke, shape)
            .padding(ThemeTokens.customPreviewPadding),
        verticalArrangement = Arrangement.spacedBy(ThemeTokens.cardGap),
    ) {
        Text(
            base.title,
            color = base.text2,
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
                    .background(shades.fill, CircleShape)
                    .border(PanelTokens.borderWidth, shades.text, CircleShape)
                    .padding(horizontal = ThemeTokens.customPillPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Inbox",
                    color = base.text1,
                    fontSize = ThemeTokens.roleCaptionSize,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text("Done", color = shades.text, fontWeight = FontWeight.SemiBold)
        }
    }
}

private val hueTrack =
    Brush.horizontalGradient(
        (0..HUE_STOPS).map { hslColor(MAX_HUE * it / HUE_STOPS, TRACK_SATURATION, TRACK_LIGHTNESS) },
    )

private const val SLIDER_LIGHTNESS = 0.5f
private const val MIN_INTENSITY = 0.1f
private const val MAX_INTENSITY = 0.9f
private const val MAX_HUE = 359f
private const val HUE_STOPS = 6
private const val TRACK_SATURATION = 0.55f
private const val TRACK_LIGHTNESS = 0.45f
