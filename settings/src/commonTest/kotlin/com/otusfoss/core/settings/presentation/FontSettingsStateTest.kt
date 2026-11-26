package com.otusfoss.core.settings.presentation

import com.otusfoss.core.designsystem.theme.typography.AppFont
import com.otusfoss.core.storage.inMemoryChoice
import kotlin.test.Test
import kotlin.test.assertEquals

class FontSettingsStateTest {
    @Test
    fun aFontIsAppliedAndRestoredForTheNextSession() {
        val store = inMemoryChoice()
        FontSettingsState(store).select(AppFont.FigtreeRounded)
        assertEquals(AppFont.FigtreeRounded, FontSettingsState(store).font)
    }

    @Test
    fun withoutASavedFontTextUsesTheSystemFont() {
        assertEquals(AppFont.System, FontSettingsState(inMemoryChoice()).font)
    }

    @Test
    fun anUnknownFontReadsAsTheSystemFont() {
        assertEquals(AppFont.System, FontSettingsState(inMemoryChoice("comic sans")).font)
        assertEquals(AppFont.GoogleSans, FontSettingsState(inMemoryChoice("googlesans")).font)
    }
}
