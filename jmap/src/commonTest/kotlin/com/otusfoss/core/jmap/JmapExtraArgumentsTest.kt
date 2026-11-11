package com.otusfoss.core.jmap

import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JmapExtraArgumentsTest {
    private val otherAccount = JsonPrimitive("other")

    @Test
    fun extraArgumentsCannotReplaceTheCheckedAccountOrStandardArguments() {
        val methods =
            listOf(
                { extra: Map<String, JsonPrimitive> -> GetMethod(MAILBOX, "d1", extra = extra) },
                { extra: Map<String, JsonPrimitive> -> QueryMethod(MAILBOX, "d1", extra = extra) },
                { extra: Map<String, JsonPrimitive> -> SetMethod(MAILBOX, "d1", extra = extra) },
            )
        val clashes = listOf("accountId", "#accountId")
        methods.forEach { method ->
            clashes.forEach { key -> assertFailsWith<IllegalArgumentException> { method(mapOf(key to otherAccount)) } }
        }
        assertFailsWith<IllegalArgumentException> { GetMethod(MAILBOX, "d1", extra = mapOf("#ids" to otherAccount)) }
        assertFailsWith<IllegalArgumentException> { QueryMethod(MAILBOX, "d1", extra = mapOf("limit" to otherAccount)) }
        assertFailsWith<IllegalArgumentException> { SetMethod(MAILBOX, "d1", extra = mapOf("destroy" to otherAccount)) }
    }

    @Test
    fun typeSpecificExtraArgumentsAreSent() {
        val position = JsonPrimitive(32)
        val query = QueryMethod(MAILBOX, "d1", extra = mapOf("position" to position)).arguments()
        assertEquals(position, query.getValue("position"))
        assertEquals(JsonPrimitive("d1"), query.getValue("accountId"))
    }
}
