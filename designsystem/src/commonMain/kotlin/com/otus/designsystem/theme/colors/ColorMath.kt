package com.otus.designsystem.theme.colors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max
import kotlin.math.min

/** An opaque color from HSL, rounded to 8 bits per channel; out-of-range values are clamped. */
public fun hslColor(
    hue: Float,
    saturation: Float,
    lightness: Float,
): Color =
    Color(
        Color
            .hsl(
                hue.mod(HUE_TURN),
                saturation.coerceIn(0f, 1f),
                lightness.coerceIn(0f, 1f),
            ).toArgb(),
    )

public fun Color.toHsl(): Hsl {
    val high = max(red, max(green, blue))
    val low = min(red, min(green, blue))
    val lightness = (high + low) / 2
    val spread = high - low
    if (spread == 0f) return Hsl(0f, 0f, lightness)
    val saturation = if (lightness > HALF) spread / (2 - high - low) else spread / (high + low)
    val sector =
        when (high) {
            red -> (green - blue) / spread + if (green < blue) SECTORS else 0f
            green -> (blue - red) / spread + 2
            else -> (red - green) / spread + 4
        }
    return Hsl(sector * HUE_TURN / SECTORS, saturation, lightness)
}

/** The WCAG contrast ratio between two opaque colors, from 1 to 21. */
public fun contrast(
    first: Color,
    second: Color,
): Float {
    val a = first.luminance()
    val b = second.luminance()
    return (max(a, b) + CONTRAST_OFFSET) / (min(a, b) + CONTRAST_OFFSET)
}

/** `text1` or `bgDark`, whichever contrasts more with [fill], for letters and symbols drawn on it. */
internal fun readableOn(fill: Color): Color = listOf(OtusColors.text1, OtusColors.bgDark).maxBy { contrast(it, fill) }

/** The color as `#RRGGBB`, upper case. */
public fun Color.toHex(): String = "#" + (toArgb() and RGB_MASK).toString(HEX).padStart(HEX_DIGITS, '0').uppercase()

/** Reads `#RRGGBB` or `RRGGBB`; null for anything else. */
public fun parseHexColor(text: String): Color? =
    text
        .trim()
        .removePrefix("#")
        .takeIf { it.length == HEX_DIGITS && it.all { char -> char.isDigit() || char.lowercaseChar() in 'a'..'f' } }
        ?.toLong(HEX)
        ?.let { Color(OPAQUE or it) }

private const val HUE_TURN = 360f
private const val SECTORS = 6f
private const val HALF = 0.5f
private const val CONTRAST_OFFSET = 0.05f
private const val RGB_MASK = 0xFFFFFF
private const val HEX = 16
private const val HEX_DIGITS = 6
private const val OPAQUE = 0xFF000000L
