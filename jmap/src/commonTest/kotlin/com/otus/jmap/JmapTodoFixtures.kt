package com.otus.jmap

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/** The `Todo` type of the RFC 8620 examples (section 5.7), kept as raw JSON. */
val TODO_TYPE = JmapType("Todo", "urn:example:todo", JsonObject.serializer())

const val TODO_SESSION_URL = "https://todo.example.org/jmap/session"

/** Accounts `x` and `y` of the RFC 8620 examples (section 5.7), and `z`, which holds no Todos. */
val TODO_SESSION =
    """
    {"capabilities": {"urn:ietf:params:jmap:core": {}, "urn:example:todo": {}},
     "accounts": {"x": {"name": "Ada", "isPersonal": true, "accountCapabilities": {"urn:example:todo": {}}},
       "y": {"name": "Team", "accountCapabilities": {"urn:example:todo": {}}},
       "z": {"name": "Archive", "accountCapabilities": {}}},
     "primaryAccounts": {"urn:example:todo": "x"}, "apiUrl": "/jmap/", "state": "s1"}
    """.trimIndent()

/** The RFC example's FilterCondition, which matches Todos with [keyword]. */
fun hasKeyword(keyword: String): JmapFilter =
    JmapFilter.Condition(buildJsonObject { put("hasKeyword", JsonPrimitive(keyword)) })

fun parseObject(json: String): JsonObject = JmapJson.parseToJsonElement(json) as JsonObject
