package com.otus.settings.presentation.components

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
import com.otus.designsystem.components.SheetFrame
import com.otus.designsystem.components.SheetHeaderAction
import com.otus.designsystem.elements.ColorSlider
import com.otus.designsystem.elements.InputField
import com.otus.designsystem.theme.Hsl
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.ThemeTokens
import com.otus.designsystem.theme.hslColor
import com.otus.designsystem.theme.parseHexColor
import com.otus.designsystem.theme.toHex
import com.otus.designsystem.theme.toHsl

/**
 * Picks any color by hue, intensity and lightness, or by hex code, starting from [initial]. Done
 * runs [onPick] with the color. With [onReset], a Reset button runs it instead, such as to put back
 * the color Otus gives a theme. However the sheet closes, it then runs [onDismiss], so the caller
 * can remove it. [footer] follows the sliders and sees the color as it changes.
 */
@Composable
internal fun ColorSheet(
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
        onDone = {
            if (resetting) onReset?.invoke() else onPick(color)
            onDismiss()
        },
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
                parseHexColor(text)?.let {
                    color = it
                    hsl = it.toHsl()
                }
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
                .border(PanelTokens.borderWidth, OtusColors.bgLight, CircleShape),
        )
        InputField("Hex", hex, onHexChange, Modifier.weight(1f), placeholder = "#4D6F92")
    }
}

private val hueTrack =
    Brush.horizontalGradient(
        (0..HUE_STOPS).map { hslColor(MAX_HUE * it / HUE_STOPS, TRACK_SATURATION, TRACK_LIGHTNESS) },
    )

private const val HALF = 0.5f
private const val MAX_HUE = 359f
private const val HUE_STOPS = 6
private const val TRACK_SATURATION = 0.55f
private const val TRACK_LIGHTNESS = 0.45f
