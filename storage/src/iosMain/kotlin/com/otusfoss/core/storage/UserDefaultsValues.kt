package com.otusfoss.core.storage

import platform.Foundation.NSUserDefaults

/** A [KeyValueStore] in [defaults], with [prefix] in front of every key. */
public class UserDefaultsValues(
    private val prefix: String,
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : KeyValueStore,
    KeyValueEditor {
    override fun string(key: String): String? = defaults.stringForKey(prefix + key)

    // boolForKey and integerForKey cannot tell a saved false or 0 from a missing value.
    override fun flag(key: String): Boolean? = saved(key)?.let { defaults.boolForKey(prefix + key) }

    override fun int(key: String): Int? = saved(key)?.let { defaults.integerForKey(prefix + key).toInt() }

    override fun edit(changes: KeyValueEditor.() -> Unit): Unit = changes()

    override fun put(
        key: String,
        value: String,
    ): Unit = defaults.setObject(value, forKey = prefix + key)

    override fun put(
        key: String,
        value: Boolean,
    ): Unit = defaults.setBool(value, forKey = prefix + key)

    override fun put(
        key: String,
        value: Int,
    ): Unit = defaults.setInteger(value.toLong(), forKey = prefix + key)

    private fun saved(key: String): Any? = defaults.objectForKey(prefix + key)
}
