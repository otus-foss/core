package com.otus.storage

/**
 * Small values saved under string keys, such as Android shared preferences or iOS user defaults.
 * Each getter returns null for a missing key, so callers pick their own defaults.
 */
public interface KeyValueStore {
    public fun string(key: String): String?

    public fun flag(key: String): Boolean?

    public fun int(key: String): Int?

    /** Applies [changes] together. */
    public fun edit(changes: KeyValueEditor.() -> Unit)
}

public interface KeyValueEditor {
    public fun put(
        key: String,
        value: String,
    )

    public fun put(
        key: String,
        value: Boolean,
    )

    public fun put(
        key: String,
        value: Int,
    )
}

public fun KeyValueStore.flag(
    key: String,
    default: Boolean,
): Boolean = flag(key) ?: default

public fun KeyValueStore.int(
    key: String,
    default: Int,
): Int = int(key) ?: default

/** The value saved under [key], read with [decode]. */
public fun <T> KeyValueStore.decoded(
    key: String,
    decode: (String) -> T,
): T? = string(key)?.let(decode)

/** The value saved under [key], read with [decode], or [default] when nothing is saved. */
public fun <T> KeyValueStore.decoded(
    key: String,
    default: T,
    decode: (String) -> T,
): T = decoded(key, decode) ?: default
