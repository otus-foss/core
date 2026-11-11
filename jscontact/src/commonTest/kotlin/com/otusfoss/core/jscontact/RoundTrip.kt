package com.otusfoss.core.jscontact

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.assertEquals

/**
 * Decodes JSContact JSON, such as the RFC 9553 examples, encodes it again, and asserts that nothing changed:
 * for a single object, an `Id[X]` map, or a whole Card. Encoding adds `@type` to every object, which the
 * examples mostly leave out, so each comparison ignores `@type`.
 */
object RoundTrip {
    /** The value of [property] in an RFC fragment such as `"media": {...}`, which lacks its outer braces. */
    fun fragment(
        text: String,
        property: String,
    ): JsonObject = CardJson.parse("{$text}").getValue(property).jsonObject

    fun <T> decodeEntries(
        serializer: KSerializer<T>,
        entries: JsonElement,
    ): Map<String, T> = JsContactJson.decodeFromJsonElement(entriesOf(serializer), entries)

    fun <T> encodeEntries(
        serializer: KSerializer<T>,
        entries: Map<String, T>,
    ): JsonObject = JsContactJson.encodeToJsonElement(entriesOf(serializer), entries).jsonObject

    /** The `@type` that encoding [value] writes. */
    fun <T> typeOf(
        serializer: KSerializer<T>,
        value: T,
    ): JsonElement? = JsContactJson.encodeToJsonElement(serializer, value).jsonObject["@type"]

    /** Asserts that one object, such as a Card `name`, survives the round trip. Returns the decoded value. */
    fun <T> assertRoundTrip(
        serializer: KSerializer<T>,
        json: JsonElement,
    ): T = roundTrip(serializer, json, expected = json).first

    /**
     * Asserts that the `Id[X]` map of [property] in the fragment [text] survives the round trip. Returns the
     * decoded entries and their encoded JSON.
     */
    fun <T> assertEntriesRoundTrip(
        serializer: KSerializer<T>,
        text: String,
        property: String,
    ): Pair<Map<String, T>, JsonObject> {
        val entries = fragment(text, property)
        val (decoded, encoded) = roundTrip(entriesOf(serializer), entries, expected = entries)
        return decoded to encoded.jsonObject
    }

    /**
     * Asserts that the Card [text] survives the round trip, with [added] members in the output and a top-level
     * `"@type": "Card"`. Returns the decoded Card.
     */
    fun assertCardRoundTrip(
        text: String,
        added: Map<String, JsonElement> = emptyMap(),
    ): Card {
        val json = CardJson.parse(text)
        val (card, encoded) = roundTrip(Card.serializer(), json, expected = JsonObject(json + added))
        assertEquals(JsonPrimitive("Card"), encoded.jsonObject["@type"])
        return card
    }

    fun withoutTypes(element: JsonElement): JsonElement =
        when (element) {
            is JsonObject -> JsonObject(element.filterKeys { it != "@type" }.mapValues { withoutTypes(it.value) })
            is JsonArray -> JsonArray(element.map(::withoutTypes))
            else -> element
        }

    /**
     * Decodes [json], encodes the value again, asserts that the output is [expected] and decodes to the same
     * value, and returns the value and the output.
     */
    private fun <T> roundTrip(
        serializer: KSerializer<T>,
        json: JsonElement,
        expected: JsonElement,
    ): Pair<T, JsonElement> {
        val decoded = JsContactJson.decodeFromJsonElement(serializer, json)
        val encoded = JsContactJson.encodeToJsonElement(serializer, decoded)
        assertEquals(withoutTypes(expected), withoutTypes(encoded))
        assertEquals(decoded, JsContactJson.decodeFromJsonElement(serializer, encoded))
        return decoded to encoded
    }

    private fun <T> entriesOf(serializer: KSerializer<T>): KSerializer<Map<String, T>> =
        MapSerializer(String.serializer(), serializer)
}
