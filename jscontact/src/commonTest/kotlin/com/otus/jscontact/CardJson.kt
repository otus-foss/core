package com.otus.jscontact

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.assertEquals

/** Reads and validates Cards for the Card tests. [RoundTrip] checks that they encode back unchanged. */
object CardJson {
    fun decode(text: String): Card = JsContactJson.decodeFromString(Card.serializer(), text)

    fun parse(text: String): JsonObject = JsContactJson.parseToJsonElement(text).jsonObject

    /** A valid Card with [members] added, a fragment such as `"kind": "org"`. */
    fun cardWith(members: String): Card = decode("""{"@type": "Card", "version": "1.0", "uid": "u1", $members}""")

    /** The paths of the problems of [cardWith] for [members]. */
    fun problemPaths(members: String): List<String> = cardWith(members).validate().map { it.path }

    /** Asserts that [cardWith] for [members] has exactly one problem, at [path]. */
    fun assertProblemAt(
        path: String,
        members: String,
    ) {
        assertEquals(listOf(path), problemPaths(members), members)
    }

    /** Asserts that [cardWith] for each of [members] is valid. */
    fun assertValid(vararg members: String) {
        members.forEach { assertEquals(emptyList(), problemPaths(it), it) }
    }
}
