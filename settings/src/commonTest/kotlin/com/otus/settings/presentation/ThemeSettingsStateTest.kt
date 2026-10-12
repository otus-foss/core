package com.otus.settings.presentation

import androidx.compose.ui.graphics.Color
import com.otus.designsystem.theme.AccentPreset
import com.otus.designsystem.theme.ThemeBase
import com.otus.designsystem.theme.ThemeChoice
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeSettingsStateTest {
    @Test
    fun aChoiceIsAppliedAndRestoredForTheNextSession() {
        val store = TestThemeStore()
        val state = ThemeSettingsState(store)
        val choice = ThemeChoice(ThemeBase.Black, Color(0xFFC75E38))
        state.select(choice)
        assertEquals(choice, state.choice)
        assertEquals(choice.palette, state.palette)
        assertEquals(choice, ThemeSettingsState(store).choice)
    }

    @Test
    fun withoutASavedChoiceTheDefaultApplies() {
        assertEquals(ThemeChoice(), ThemeSettingsState(TestThemeStore()).choice)
    }

    @Test
    fun anUnreadablePartFallsBackToItsDefault() {
        assertEquals(ThemeChoice(ThemeBase.Light), decodeThemeChoice("light purple"))
        assertEquals(ThemeChoice(accent = AccentPreset.Green.color), decodeThemeChoice("sepia #3F8F6B"))
        assertEquals(ThemeChoice(), decodeThemeChoice(""))
    }

    private class TestThemeStore : ThemePreferenceStore {
        private var value: String? = null

        override fun read(): String? = value

        override fun write(value: String) {
            this.value = value
        }
    }
}
