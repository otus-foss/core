package com.otus.settings.presentation

import com.otus.designsystem.theme.AppIconStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class AppIconSettingsStateTest {
    @Test
    fun aStyleIsAppliedAndRestoredForTheNextSession() {
        val store = TestAppIconStore()
        AppIconSettingsState(store).select(AppIconStyle.Monochrome)
        assertEquals(AppIconStyle.Monochrome, AppIconSettingsState(store).style)
    }

    @Test
    fun withoutASavedStyleTheIconUsesTheAccent() {
        assertEquals(AppIconStyle.Accent, AppIconSettingsState(TestAppIconStore()).style)
        assertEquals(AppIconStyle.Accent, decodeAppIconStyle("sparkly"))
    }

    private class TestAppIconStore : AppIconPreferenceStore {
        private var value: String? = null

        override fun read(): String? = value

        override fun write(value: String) {
            this.value = value
        }
    }
}
