package com.otus.storage

/** One choice, such as the theme or the app icon, saved as text under [key] in [values]. */
public class StoredChoice(
    private val values: KeyValueStore,
    private val key: String,
) {
    public fun read(): String? = values.string(key)

    public fun write(value: String): Unit = values.edit { put(key, value) }
}

/** A choice kept only in memory, holding [value] at first, for previews and tests. */
public fun inMemoryChoice(value: String? = null): StoredChoice =
    StoredChoice(InMemoryKeyValueStore(value?.let { mapOf(CHOICE to it) }.orEmpty()), CHOICE)

private const val CHOICE = "choice"
