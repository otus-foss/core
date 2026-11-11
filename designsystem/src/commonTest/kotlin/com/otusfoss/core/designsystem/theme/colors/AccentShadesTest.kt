package com.otusfoss.core.designsystem.theme.colors

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccentShadesTest {
    @Test
    fun theDefaultChoiceKeepsTheOriginalOtusPalette() {
        assertEquals(OtusPalette.Otus, ThemeChoice().palette)
    }

    @Test
    fun everyPresetStaysReadableInEveryTheme() {
        ThemeBase.entries.forEach { base ->
            AccentPreset.entries.forEach { preset ->
                val shades = base.accentShades(preset.color)
                val where = "${preset.name} on ${base.name}"
                assertTrue(contrast(base.text1, shades.fill) >= READABLE, "fill under text, $where")
                assertTrue(contrast(shades.text, base.page) >= READABLE, "text on the page, $where")
                assertTrue(contrast(shades.text, base.raised) >= READABLE, "text on raised surfaces, $where")
                assertEquals(preset.color, shades.regular, "outline, $where")
            }
        }
    }

    @Test
    fun aLightThemeGetsAPaleFillAndADarkerText() {
        val orange = AccentPreset.Orange.color
        val shades = ThemeBase.Light.accentShades(orange)
        assertTrue(shades.fill.toHsl().lightness > orange.toHsl().lightness)
        assertTrue(shades.text.toHsl().lightness < orange.toHsl().lightness)
    }

    @Test
    fun aDarkColorIsLightenedForTextOnBlack() {
        val navy = Color(0xFF1A2B6D)
        val shades = ThemeBase.Black.accentShades(navy)
        assertTrue(shades.text.toHsl().lightness > navy.toHsl().lightness)
    }

    @Test
    fun aReadableColorIsLeftAsPicked() {
        val sky = AccentPreset.Sky.color
        val shades = ThemeBase.Black.accentShades(sky)
        assertEquals(sky, shades.text)
    }

    @Test
    fun thePaletteTakesTheThemeSurfacesAndTheAccentShades() {
        val choice = ThemeChoice(ThemeBase.Ink, AccentPreset.Green.color)
        val palette = choice.palette
        assertEquals(ThemeBase.Ink.page, palette.bgRegular)
        assertEquals(ThemeBase.Ink.raised, palette.bgDark)
        assertEquals(choice.shades.fill, palette.accentDark)
        assertEquals(choice.shades.text, palette.accentLight)
        assertFalse(palette.isLight)
        assertTrue(ThemeChoice(ThemeBase.Light).palette.isLight)
    }

    @Test
    fun hexCodesRoundTrip() {
        assertEquals("#C75E38", Color(0xFFC75E38).toHex())
        assertEquals(Color(0xFFC75E38), parseHexColor("#c75e38"))
        assertEquals(Color(0xFF000000), parseHexColor(" 000000 "))
        assertNull(parseHexColor("#C75E3"))
        assertNull(parseHexColor("#C75E3G"))
    }

    @Test
    fun hslRoundTripsThroughColors() {
        val hsl = Color(0xFFC75E38).toHsl()
        assertEquals(Color(0xFFC75E38), hslColor(hsl.hue, hsl.saturation, hsl.lightness))
    }

    private companion object {
        const val READABLE = 4.5f
    }
}
