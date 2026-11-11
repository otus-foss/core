package com.otusfoss.core.jscontact

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/** Reads Cards for the Card tests. [RoundTrip] checks that they encode back unchanged. */
object CardJson {
    fun decode(text: String): Card = JsContactJson.decodeFromString(Card.serializer(), text)

    fun parse(text: String): JsonObject = JsContactJson.parseToJsonElement(text).jsonObject

    /** A valid Card with [members] added, a fragment such as `"kind": "org"`. */
    fun cardWith(members: String): Card = decode("""{"@type": "Card", "version": "1.0", "uid": "u1", $members}""")
}
