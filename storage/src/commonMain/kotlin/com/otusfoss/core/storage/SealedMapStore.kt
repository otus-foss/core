package com.otusfoss.core.storage

/**
 * Keeps a string map in [store] as one value sealed by [cipher], so neither its keys nor its
 * values can be read from the device's files. [name] ties the sealed value to this store.
 *
 * A store written before it was sealed is read as it is and sealed at once. A sealed value that no
 * longer opens, as after the device's key was lost, reads as an empty map.
 */
public class SealedMapStore(
    private val store: StringMapStore,
    private val cipher: TextCipher,
    private val name: String,
) : StringMapStore {
    override fun read(): Map<String, String> {
        val saved = store.read()
        val sealed = saved[SEALED_KEY]
        return when {
            sealed != null -> cipher.open(sealed, name)?.let(::decodeEntries).orEmpty()
            saved.isEmpty() -> saved
            else -> saved.also(::write)
        }
    }

    override fun write(values: Map<String, String>) {
        store.write(mapOf(SEALED_KEY to cipher.seal(encodeEntries(values), name)))
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
