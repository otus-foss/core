package com.otusfoss.core.storage

import android.content.Context

/**
 * A [StringMapStore] in the app-private shared preferences file [name], replaced whole on each
 * write. With [commit], a write reaches the disk before it returns.
 */
public class PreferenceMap(
    context: Context,
    name: String,
    private val commit: Boolean = false,
) : StringMapStore {
    private val preferences = context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)

    override fun read(): Map<String, String> =
        preferences.all
            .mapNotNull { (key, value) -> (value as? String)?.let { key to it } }
            .toMap()

    override fun write(values: Map<String, String>) {
        val editor = preferences.edit().clear()
        values.forEach { (key, value) -> editor.putString(key, value) }
        if (commit) editor.commit() else editor.apply()
    }
}
