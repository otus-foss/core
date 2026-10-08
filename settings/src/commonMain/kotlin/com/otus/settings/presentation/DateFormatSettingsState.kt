package com.otus.settings.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.otus.dateformat.DateFormatPreference
import com.otus.dateformat.DateFormatPreferenceStore

/** A future settings screen consumes preference and calls select, without owning persistence. */
public class DateFormatSettingsState(
    private val store: DateFormatPreferenceStore? = null,
    initialPreference: DateFormatPreference = store?.read() ?: DateFormatPreference.DeviceLocale,
) {
    public var preference: DateFormatPreference by mutableStateOf(initialPreference)
        private set

    public fun select(preference: DateFormatPreference) {
        store?.write(preference)
        this.preference = preference
    }
}
