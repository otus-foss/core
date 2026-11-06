package com.otus.designsystem.shared.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.otus.designsystem.shared.input.elements.InputField
import com.otus.designsystem.shared.overlays.components.SheetConfirm
import com.otus.designsystem.shared.overlays.components.SheetFrame
import com.otus.designsystem.shared.overlays.components.SheetHeaderAction
import com.otus.designsystem.shared.reorder.components.HALF
import com.otus.designsystem.shared.settings.elements.ColorSlider
import com.otus.designsystem.theme.colors.Hsl
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.colors.hslColor
import com.otus.designsystem.theme.colors.parseHexColor
import com.otus.designsystem.theme.colors.toHex
import com.otus.designsystem.theme.colors.toHsl
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.tokens.settings.ThemeTokens

/**
 * Picks any color by hue, intensity and lightness, or by hex code, starting from [initial]. Done
 * runs [onPick] with the color. With [onReset], a Reset button runs it instead, such as to put back
 * the color Otus gives a theme. However the sheet closes, it then runs [onDismiss], so the caller
 * can remove it. [footer] follows the sliders and sees the color as it changes.
 */
@Composable
public fun ColorSheet(
    title: String,
    initial: Color,
    onPick: (Color) -> Unit,
    onDismiss: () -> Unit,
    onReset: (() -> Unit)? = null,
    footer: @Composable ColumnScope.(Color) -> Unit = {},
) {
    var color by remember { mutableStateOf(initial) }
    var hex by remember { mutableStateOf(initial.toHex()) }
    var hsl by remember { mutableStateOf(initial.toHsl()) }
    var resetting by remember { mutableStateOf(false) }
    val slide = { changed: Hsl ->
        hsl = changed
        color = hslColor(changed.hue, changed.saturation, changed.lightness)
        hex = color.toHex()
    }
    SheetFrame(
        title,
        onDismiss = onDismiss,
        done =
            SheetConfirm("Done", {
                if (resetting) onReset?.invoke() else onPick(color)
                onDismiss()
            }),
        leadingAction =
            onReset?.let {
                { done ->
                    SheetHeaderAction("Reset", {
                        resetting = true
                        done()
                    })
                }
            },
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SheetTokens.inset)
                .padding(bottom = SheetTokens.inset),
            verticalArrangement = Arrangement.spacedBy(SheetTokens.inset),
        ) {
            HexRow(color, hex) { text ->
                hex = text
                val parsed = parseHexColor(text) ?: return@HexRow
                color = parsed
                hsl = parsed.toHsl()
            }
            ColorSlider(hsl.hue, { slide(hsl.copy(hue = it)) }, hueTrack, "Hue", range = 0f..MAX_HUE)
            ColorSlider(
                hsl.saturation,
                { slide(hsl.copy(saturation = it)) },
                Brush.horizontalGradient(listOf(0f, 1f).map { hslColor(hsl.hue, it, hsl.lightness) }),
                "Intensity",
            )
            ColorSlider(
                hsl.lightness,
                { slide(hsl.copy(lightness = it)) },
                Brush.horizontalGradient(listOf(0f, HALF, 1f).map { hslColor(hsl.hue, hsl.saturation, it) }),
                "Lightness",
            )
            footer(color)
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
                .border(Sizes.borderWidth, OtusColors.bgLight, CircleShape),
        )
        InputField("Hex", hex, onHexChange, Modifier.weight(1f), placeholder = "#5A7EA4")
    }
}

private val hueTrack =
    Brush.horizontalGradient(
        (0..HUE_STOPS).map { hslColor(MAX_HUE * it / HUE_STOPS, TRACK_SATURATION, TRACK_LIGHTNESS) },
    )

private const val MAX_HUE = 359f
private const val HUE_STOPS = 6
private const val TRACK_SATURATION = 0.55f
private const val TRACK_LIGHTNESS = 0.45f
