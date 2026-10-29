package com.otus.settings.presentation

import com.otus.designsystem.theme.brand.AppIconStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class AppIconSettingsStateTest {
    @Test
    fun aStyleIsAppliedAndRestoredForTheNextSession() {
        val store = MemoryPreferenceStore()
        AppIconSettingsState(store).select(AppIconStyle.Monochrome)
        assertEquals(AppIconStyle.Monochrome, AppIconSettingsState(store).style)
    }

    @Test
    fun withoutASavedStyleTheIconUsesTheAccent() {
        assertEquals(AppIconStyle.Accent, AppIconSettingsState(MemoryPreferenceStore()).style)
        assertEquals(AppIconStyle.Accent, decodeAppIconStyle("sparkly"))
    }
}
