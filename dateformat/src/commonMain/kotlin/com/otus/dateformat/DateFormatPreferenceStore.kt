package com.otus.dateformat

/** App-local preferences. Installed Mail and Calendar apps do not share this storage. */
public interface DateFormatPreferenceStore {
    public fun read(): DateFormatPreference

    public fun write(preference: DateFormatPreference)
}
