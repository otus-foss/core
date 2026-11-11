package com.otusfoss.core.settings.presentation

import com.otusfoss.core.designsystem.theme.brand.AppIconStyle
import com.otusfoss.core.storage.inMemoryChoice
import kotlin.test.Test
import kotlin.test.assertEquals

class AppIconSettingsStateTest {
    @Test
    fun aStyleIsAppliedAndRestoredForTheNextSession() {
        val store = inMemoryChoice()
        AppIconSettingsState(store).select(AppIconStyle.Monochrome)
        assertEquals(AppIconStyle.Monochrome, AppIconSettingsState(store).style)
    }

    @Test
    fun withoutASavedStyleTheIconUsesTheAccent() {
        assertEquals(AppIconStyle.Accent, AppIconSettingsState(inMemoryChoice()).style)
        assertEquals(AppIconStyle.Accent, AppIconSettingsState(inMemoryChoice("sparkly")).style)
    }
}
