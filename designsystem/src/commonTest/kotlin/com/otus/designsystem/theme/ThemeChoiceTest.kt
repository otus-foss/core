package com.otus.designsystem.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ThemeChoiceTest {
    @Test
    fun changingAColorMakesACustomThemeAndLeavesTheBuiltInOneAlone() {
        val page = Color(0xFF101828)
        val choice = ThemeChoice(ThemeBase.Graphite).withColor(ThemeColor.Page, page)
        assertEquals(listOf(CustomTheme(1, ThemeBase.Graphite, mapOf(ThemeColor.Page to page))), choice.customThemes)
        assertEquals(1, choice.customId)
        assertEquals(page, choice.palette.bgRegular)
        assertEquals(ThemeBase.Graphite.raised, choice.palette.bgDark)
        assertEquals(ThemeBase.Graphite.page, choice.withBase(ThemeBase.Graphite).palette.bgRegular)
    }

    @Test
    fun aSelectedCustomThemeIsChangedInPlace() {
        val choice =
            ThemeChoice(ThemeBase.Ink)
                .withColor(ThemeColor.Page, Color(0xFF101828))
                .withColor(ThemeColor.Text, Color.White)
        assertEquals(1, choice.customThemes.size)
        assertEquals(2, choice.customColors.size)
    }

    @Test
    fun aChangeOnABuiltInThemeAddsAnotherCustomTheme() {
        val first = ThemeChoice().withColor(ThemeColor.Page, Color(0xFF101828))
        val second = first.withBase(ThemeBase.Black).withColor(ThemeColor.Text, Color.White)
        assertEquals(listOf(1, 2), second.customThemes.map { it.id })
        assertEquals(ThemeBase.Black, second.origin)
        assertEquals(Color(0xFF101828), second.withCustomTheme(1).palette.bgRegular)
    }

    @Test
    fun aCustomThemeStaysUntilItIsDeleted() {
        val custom = ThemeChoice(ThemeBase.Ink).withColor(ThemeColor.Borders, Color(0xFF333333))
        val reset = custom.withColor(ThemeColor.Borders, null)
        assertEquals(listOf(CustomTheme(1, ThemeBase.Ink)), reset.customThemes)
        assertEquals(1, reset.customId)
        val deleted = reset.withoutCustomTheme(1)
        assertEquals(ThemeChoice(ThemeBase.Ink), deleted)
    }

    @Test
    fun deletingAnotherCustomThemeKeepsTheSelection() {
        val two =
            ThemeChoice()
                .withColor(
                    ThemeColor.Page,
                    Color.Black,
                ).withBase(ThemeBase.Ink)
                .withColor(ThemeColor.Text, Color.White)
        val kept = two.withoutCustomTheme(1)
        assertEquals(2, kept.customId)
        assertEquals(listOf(2), kept.customThemes.map { it.id })
    }

    @Test
    fun theAccentShadesFollowChangedSurfaces() {
        val choice = ThemeChoice(ThemeBase.Otus, AccentPreset.Violet.color)
        val brighter = choice.withColor(ThemeColor.Page, Color(0xFF6A6A6A))
        assertNotEquals(choice.shades.text, brighter.shades.text)
        assertTrue(contrast(brighter.shades.text, brighter.palette.bgRegular) >= READABLE)
    }

    @Test
    fun aBrightPageMakesTheThemeLight() {
        assertTrue(ThemeChoice().withColor(ThemeColor.Page, Color(0xFFFAFAFA)).palette.isLight)
    }

    @Test
    fun anAccentShadeCanBeChangedUntilANewAccentIsPicked() {
        val fill = Color(0xFF203040)
        val choice = ThemeChoice().withColor(ThemeColor.AccentFill, fill).withColor(ThemeColor.Text, Color.White)
        assertEquals(fill, choice.palette.accentDark)
        val picked = choice.withAccent(AccentPreset.Green.color)
        assertNotEquals(fill, picked.palette.accentDark)
        assertEquals(Color.White, picked.palette.text1)
    }

    @Test
    fun pickingAColorTheThemeAlreadyHasChangesNothing() {
        val changed = ThemeChoice(ThemeBase.Ink).withColor(ThemeColor.Borders, Color(0xFF333333))
        assertTrue(changed.withColor(ThemeColor.Borders, ThemeBase.Ink.stroke).customColors.isEmpty())
        assertEquals(ThemeChoice(), ThemeChoice().withColor(ThemeColor.AccentText, OtusPalette.Otus.accentLight))
        assertEquals(ThemeChoice(), ThemeChoice().withColor(ThemeColor.Page, null))
    }

    @Test
    fun inkIsDarkerThanOtusWithCloserSurfaces() {
        val ink = ThemeBase.Ink
        val otus = ThemeBase.Otus
        assertTrue(ink.page.toHsl().lightness < otus.page.toHsl().lightness)
        assertTrue(contrast(ink.raised, ink.page) < contrast(otus.raised, otus.page))
        assertTrue(contrast(ink.light, ink.page) < contrast(otus.light, otus.page))
        assertTrue(contrast(ink.text3, ink.page) >= READABLE)
    }

    private companion object {
        const val READABLE = 4.5f
    }
}
