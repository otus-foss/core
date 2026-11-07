package com.otus.settings.presentation

import com.otus.dateformat.DateFormatPreference
import com.otus.storage.StoredChoice

/** The user's date format; the appearance page calls [select]. An unknown saved value follows the device. */
public class DateFormatSettingsState(
    store: StoredChoice,
) : StoredChoiceState<DateFormatPreference>(
        store,
        DateFormatPreference.DeviceLocale,
        { saved -> DateFormatPreference.entries.firstOrNull { it.storageValue == saved } },
        { it.storageValue },
    ) {
    public val preference: DateFormatPreference get() = value
}
