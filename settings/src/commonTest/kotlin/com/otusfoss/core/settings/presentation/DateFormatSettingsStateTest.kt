package com.otusfoss.core.settings.presentation

import com.otusfoss.core.dateformat.DateFormatPreference
import com.otusfoss.core.storage.InMemoryKeyValueStore
import com.otusfoss.core.storage.KeyValueEditor
import com.otusfoss.core.storage.KeyValueStore
import com.otusfoss.core.storage.StoredChoice
import com.otusfoss.core.storage.inMemoryChoice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DateFormatSettingsStateTest {
    @Test
    fun selectingAFormatUpdatesStateAndRestoresItForTheNextSession() {
        val store = inMemoryChoice()
        val state = DateFormatSettingsState(store)
        state.select(DateFormatPreference.DayMonthYearSlashes)
        assertEquals(DateFormatPreference.DayMonthYearSlashes, state.preference)
        assertEquals(state.preference, DateFormatSettingsState(store).preference)
        state.select(DateFormatPreference.DeviceLocale)
        assertEquals(DateFormatPreference.DeviceLocale, DateFormatSettingsState(store).preference)
    }

    @Test
    fun appInstancesKeepTheirOwnPreferences() {
        DateFormatSettingsState(inMemoryChoice()).select(DateFormatPreference.MonthDayYearDots)
        assertEquals(DateFormatPreference.DeviceLocale, DateFormatSettingsState(inMemoryChoice()).preference)
    }

    @Test
    fun anUnknownSavedValueFollowsTheDevice() {
        assertEquals(DateFormatPreference.DeviceLocale, DateFormatSettingsState(inMemoryChoice("unknown")).preference)
    }

    @Test
    fun persistenceFailureDoesNotReportAnUnsavedSelectionAsActive() {
        val failing =
            object : KeyValueStore by InMemoryKeyValueStore() {
                override fun edit(changes: KeyValueEditor.() -> Unit) = error("Storage unavailable")
            }
        val state = DateFormatSettingsState(StoredChoice(failing, "date_format"))
        assertFailsWith<IllegalStateException> { state.select(DateFormatPreference.DayMonthYearDots) }
        assertEquals(DateFormatPreference.DeviceLocale, state.preference)
    }
}
