package com.otus.settings.presentation

import com.otus.dateformat.DateFormatPreference
import com.otus.storage.InMemoryKeyValueStore
import com.otus.storage.KeyValueEditor
import com.otus.storage.KeyValueStore
import com.otus.storage.StoredChoice
import com.otus.storage.inMemoryChoice
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
