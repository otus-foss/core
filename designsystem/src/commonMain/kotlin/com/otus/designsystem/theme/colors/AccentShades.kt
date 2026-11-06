package com.otus.designsystem.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlin.math.min

/**
 * The three roles one accent color plays in a theme: [fill] behind selected items, [regular] for
 * outlines, and [text] for accent text and rings.
 */
@Immutable
public data class AccentShades(
    public val fill: Color,
    public val regular: Color,
    public val text: Color,
)

/**
 * Derives [AccentShades] from [accent] so text stays readable on this theme: [ThemeBase.text1]
 * on [AccentShades.fill], and [AccentShades.text] on the page and raised surfaces, at 4.5:1 or
 * more. A light theme gets a pale fill under its dark text. Otus blue on [ThemeBase.Otus] keeps
 * the original hand-tuned shades.
 */
internal fun ThemeBase.accentShades(accent: Color): AccentShades = accentShades(accent, page, raised, text1)

/** [ThemeBase.accentShades] on surfaces the user may have changed. */
internal fun accentShades(
    accent: Color,
    page: Color,
    raised: Color,
    text1: Color,
): AccentShades {
    val otus = ThemeBase.Otus
    val otusDefaults = listOf(AccentPreset.OtusBlue.color, otus.page, otus.raised, otus.text1)
    if (listOf(accent, page, raised, text1) == otusDefaults) return otusBlueShades
    val isLight = page.isLightPage
    val (hue, saturation, lightness) = accent.toHsl()
    val readable = { shade: Color -> contrast(shade, raised) >= MIN_CONTRAST && contrast(shade, page) >= MIN_CONTRAST }
    val text =
        firstFitting(lightness, if (isLight) -STEP else STEP, TEXT_STEPS) { readable(hslColor(hue, saturation, it)) }
    val fillSaturation = if (isLight) min(saturation, LIGHT_FILL_MAX_SATURATION) else saturation
    val fill =
        if (isLight) {
            firstFitting(LIGHT_FILL_LIGHTNESS, STEP, LIGHT_FILL_STEPS) {
                contrast(text1, hslColor(hue, fillSaturation, it)) >= LIGHT_FILL_CONTRAST
            }
        } else {
            firstFitting(lightness * DARK_FILL_FACTOR, -STEP, DARK_FILL_STEPS) {
                contrast(text1, hslColor(hue, fillSaturation, it)) >= MIN_CONTRAST
            }
        }
    return AccentShades(
        fill = hslColor(hue, fillSaturation, fill),
        regular = accent,
        text = hslColor(hue, saturation, text),
    )
}

/** The first of [count] lightness values from [start], [step] apart, that [fits]; else the next one. */
private fun firstFitting(
    start: Float,
    step: Float,
    count: Int,
    fits: (Float) -> Boolean,
): Float = (0 until count).asSequence().map { start + step * it }.firstOrNull(fits) ?: (start + step * count)

private val otusBlueShades =
    AccentShades(
        fill = OtusPalette.Otus.accentDark,
        regular = OtusPalette.Otus.accentRegular,
        text = OtusPalette.Otus.accentLight,
    )

private const val MIN_CONTRAST = 4.5f
private const val STEP = 0.01f
private const val TEXT_STEPS = 60
private const val DARK_FILL_FACTOR = 0.78f
private const val DARK_FILL_STEPS = 40
private const val LIGHT_FILL_LIGHTNESS = 0.88f
private const val LIGHT_FILL_STEPS = 20
private const val LIGHT_FILL_MAX_SATURATION = 0.7f
private const val LIGHT_FILL_CONTRAST = 7f
