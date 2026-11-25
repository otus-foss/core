package com.otusfoss.core.storage

import kotlin.concurrent.Volatile

/**
 * Keeps a string map in [store] as one value sealed by [cipher], so neither its keys nor its
 * values can be read from the device's files. [name] ties the sealed value to this store.
 *
 * A store saved before sealing began is taken in and sealed when it is built by the process that
 * made the device's first key. Any other start reads an unsealed store as empty, so a store
 * planted in the files is never used. A sealed value that no longer opens, as after the device's
 * key was lost, reads as an empty map. While the key lasts for this process only, writes stay in
 * memory, so they never replace what the kept key sealed.
 */
public class SealedMapStore(
    private val store: StringMapStore,
    private val cipher: TextCipher,
    private val name: String,
) : StringMapStore {
    @Volatile
    private var unsaved: Map<String, String>? = null

    init {
        if (cipher.keyLife == KeyLife.First) {
            store.read().takeUnless { SEALED_KEY in it || it.isEmpty() }?.let(::write)
        }
    }

    override fun read(): Map<String, String> =
        unsaved ?: store
            .read()[SEALED_KEY]
            ?.let { cipher.open(it, name) }
            ?.let(::decodeEntries)
            .orEmpty()

    override fun write(values: Map<String, String>) {
        if (cipher.keyLife == KeyLife.ProcessOnly) {
            unsaved = values
        } else {
            store.write(mapOf(SEALED_KEY to cipher.seal(encodeEntries(values), name)))
        }
    }

    private companion object {
        const val SEALED_KEY = "sealed"
    }
}

/** Each key and value as its length, a colon, and its text, so any text reads back unchanged. */
internal fun encodeEntries(values: Map<String, String>): String =
    buildString {
        values.forEach { (key, value) ->
            append(key.length).append(':').append(key)
            append(value.length).append(':').append(value)
        }
    }

/** The map [encodeEntries] wrote; null when [text] is not one. */
internal fun decodeEntries(text: String): Map<String, String>? {
    val parts = mutableListOf<String>()
    var position = 0
    while (position < text.length) {
        val end = partEnd(text, position) ?: return null
        parts += text.substring(text.indexOf(':', position) + 1, end)
        position = end
    }
    return parts.takeIf { it.size % 2 == 0 }?.chunked(2)?.associate { (key, value) -> key to value }
}

/** Where the part that starts with its length at [position] ends; null when no whole part does. */
private fun partEnd(
    text: String,
    position: Int,
): Int? {
    val colon = text.indexOf(':', position)
    val length = text.substring(position, colon.coerceAtLeast(position)).toIntOrNull()?.takeIf { it >= 0 }
    return length?.let { colon + 1 + it }?.takeIf { it <= text.length }
}
