package com.otus.jmap.contacts

import com.otus.jmap.JmapPatchBuilder
import com.otus.jscontact.Card
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/**
 * One Card property in a [ContactCardPatch] (RFC 9553, section 2). A value is written exactly as a
 * whole [Card] writes it, so objects carry their `@type` and times are canonical UTCDateTime.
 */
public open class CardPatchProperty<T> internal constructor(
    internal val patch: JmapPatchBuilder,
    internal val name: String,
    // Encoding through a one-property Card reuses the jscontact serializers, which are internal.
    private val wrap: (T) -> Card,
) {
    /**
     * Replaces the whole property with [value]. A property that holds an array, such as the name
     * components, can only change this way: a patch cannot point into an array (RFC 8620, section 5.3).
     */
    public fun set(value: T) {
        patch.set(json(value), name)
    }

    /** Removes the property, or resets it to its default where RFC 9553 has one, as for `kind`. */
    public fun remove() {
        patch.remove(name)
    }

    internal fun json(value: T): JsonElement = wrap(value).toJsonObject().getValue(name)
}

/**
 * A Card property that maps keys to objects, such as `emails` (`Id[EmailAddress]`) or
 * `localizations` (RFC 9553, sections 1.4.1 and 2). [put] and [remove] patch one entry and leave the
 * others as they are, which keeps entries this client does not know. They need the map on the
 * server: when the card lacks it, [set] the whole map instead.
 */
public class CardPatchMap<V> internal constructor(
    patch: JmapPatchBuilder,
    name: String,
    wrap: (Map<String, V>) -> Card,
) : CardPatchProperty<Map<String, V>>(patch, name, wrap) {
    /** Adds the entry [key], or replaces it whole. */
    public fun put(
        key: String,
        value: V,
    ) {
        patch.set(json(mapOf(key to value)).jsonObject.getValue(key), name, key)
    }

    /** Removes the entry [key]. */
    public fun remove(key: String) {
        patch.remove(name, key)
    }
}

/**
 * A Card property that is a set of strings, written as a map whose values are all true, such as
 * `members`, `keywords`, or the JMAP `addressBookIds` (RFC 9553, section 2; RFC 9610, section 3).
 * [add] and [remove] patch one key and need the set on the server: when the card lacks it, [set]
 * the whole set instead.
 */
public class CardPatchSet internal constructor(
    patch: JmapPatchBuilder,
    name: String,
    wrap: (Map<String, Boolean>) -> Card,
) : CardPatchProperty<Set<String>>(patch, name, { keys -> wrap(keys.associateWith { true }) }) {
    /** Adds [key] to the set. */
    public fun add(key: String) {
        patch.set(JsonPrimitive(true), name, key)
    }

    /** Removes [key] from the set. */
    public fun remove(key: String) {
        patch.remove(name, key)
    }
}
