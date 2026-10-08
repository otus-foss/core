package com.otus.dateformat

import platform.Foundation.NSUserDefaults

public class IosDateFormatPreferenceStore(
    private val preferences: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : DateFormatPreferenceStore {
    override fun read(): DateFormatPreference =
        DateFormatPreference.fromStorageValue(preferences.stringForKey(PREFERENCE_KEY))

    override fun write(preference: DateFormatPreference) {
        preferences.setObject(preference.storageValue, forKey = PREFERENCE_KEY)
    }

    private companion object {
        const val PREFERENCE_KEY = "otus.date_format"
    }
}
