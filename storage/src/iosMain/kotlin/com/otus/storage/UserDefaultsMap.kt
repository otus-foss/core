package com.otus.storage

import platform.Foundation.NSUserDefaults

/** A [StringMapStore] kept as one dictionary under [key] in [defaults]. Non-string entries are skipped. */
public class UserDefaultsMap(
    private val key: String,
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : StringMapStore {
    override fun read(): Map<String, String> =
        defaults
            .dictionaryForKey(key)
            .orEmpty()
            .mapNotNull { (name, value) -> if (name is String && value is String) name to value else null }
            .toMap()

    override fun write(values: Map<String, String>) {
        defaults.setObject(values, forKey = key)
    }
}
