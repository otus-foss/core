package com.otus.settings.presentation

import androidx.compose.ui.graphics.Color
import com.otus.designsystem.theme.colors.AccentPreset
import com.otus.designsystem.theme.colors.CustomTheme
import com.otus.designsystem.theme.colors.ThemeBase
import com.otus.designsystem.theme.colors.ThemeChoice
import com.otus.designsystem.theme.colors.ThemeColor
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeSettingsStateTest {
    @Test
    fun aChoiceIsAppliedAndRestoredForTheNextSession() {
        val store = MemoryPreferenceStore()
        val state = ThemeSettingsState(store)
        val choice = ThemeChoice(ThemeBase.Black, Color(0xFFC75E38))
        state.select(choice)
        assertEquals(choice, state.choice)
        assertEquals(choice.palette, state.palette)
        assertEquals(choice, ThemeSettingsState(store).choice)
    }

    @Test
    fun changedColorsOfEveryThemeAreRestored() {
        val store = MemoryPreferenceStore()
        val choice =
            ThemeChoice(ThemeBase.Ink)
                .withColor(ThemeColor.Page, Color(0xFF101011))
                .withBase(ThemeBase.Light)
                .withColor(ThemeColor.AccentText, Color(0xFF123456))
                .withColor(ThemeColor.AccentText, null)
                .withName(2, "Night 100%20 ink")
        ThemeSettingsState(store).select(choice)
        assertEquals(choice, ThemeSettingsState(store).choice)
    }

    @Test
    fun withoutASavedChoiceTheDefaultApplies() {
        assertEquals(ThemeChoice(), ThemeSettingsState(MemoryPreferenceStore()).choice)
    }

    @Test
    fun anUnreadablePartFallsBackToItsDefault() {
        assertEquals(ThemeChoice(ThemeBase.Light), decodeThemeChoice("light purple"))
        assertEquals(ThemeChoice(accent = AccentPreset.Green.color), decodeThemeChoice("sepia #3F8F6B"))
        assertEquals(ThemeChoice(accent = AccentPreset.Green.color), decodeThemeChoice("graphite #3F8F6B"))
        assertEquals(ThemeChoice(), decodeThemeChoice(""))
        assertEquals(
            ThemeChoice(
                customThemes = listOf(CustomTheme(1, ThemeBase.Ink, mapOf(ThemeColor.Page to Color(0xFF101011)))),
            ),
            decodeThemeChoice(
                "otus #4D6F92 custom=2 1:ink 1:page=#101011 1:glow=#000000 1:text=blue 2:sepia 3:page=#000000",
            ),
        )
    }
}
