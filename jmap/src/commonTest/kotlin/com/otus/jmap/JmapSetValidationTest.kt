package com.otus.jmap

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class JmapSetValidationTest {
    private val objectValue = JsonObject(emptyMap())

    @Test
    fun missingStateOrOperationOutcomesNeverReportSuccess() {
        val methods =
            listOf(
                SetMethod(MAILBOX, "d1", create = mapOf("new" to objectValue)),
                SetMethod(MAILBOX, "d1", update = mapOf("a" to objectValue)),
                SetMethod(MAILBOX, "d1", destroy = listOf("a")),
            )
        for (method in methods) {
            for (body in listOf("{}", """{"newState":"s2"}""")) {
                assertFailsWith<JmapException.NotJmap> { decode(method, body) }
            }
        }
    }

    @Test
    fun conflictingOutcomesAndCreatedObjectsWithoutIdsAreRejected() {
        val method = SetMethod(MAILBOX, "d1", create = mapOf("new" to objectValue))
        val malformed =
            listOf(
                """{"newState":"s2","created":{"new":{}}}""",
                """{"newState":"s2","created":{"new":{"id":17}}}""",
                """{"newState":"s2","created":{"new":{"id":"a"}},"notCreated":{"new":{"type":"forbidden"}}}""",
            )
        malformed.forEach { assertFailsWith<JmapException.NotJmap> { decode(method, it) } }
        assertFailsWith<JmapException.NotJmap> {
            decode(
                SetMethod(MAILBOX, "d1", destroy = listOf("a")),
                """{"newState":"s2","destroyed":["a"],"notDestroyed":{"a":{"type":"forbidden"}}}""",
            )
        }
    }

    @Test
    fun partialFailuresAndExtraServerUpdatesArePreserved() {
        val result =
            decode(
                SetMethod(MAILBOX, "d1", update = mapOf("a" to objectValue, "b" to objectValue)),
                """{"newState":"s2","updated":{"a":null,"serverChanged":{}},"notUpdated":{"b":{"type":"forbidden"}}}""",
            )
        assertEquals(setOf("a", "serverChanged"), result.updated.keys)
        assertEquals("forbidden", result.notUpdated.getValue("b").type)
        assertNull(decode(SetMethod(MAILBOX, "d1"), """{"newState":"s2"}""").firstError)
    }

    @Test
    fun creationReferencesDoNotRejectResolvedServerIds() {
        val result =
            decode(
                SetMethod(MAILBOX, "d1", create = mapOf("new" to objectValue), update = mapOf("#new" to objectValue)),
                """{"newState":"s2","created":{"new":{"id":"a"}},"updated":{"a":null}}""",
            )
        assertEquals("a", result.createdId("new"))
        // The prior method's creation map is not available to this decoder.
        assertNull(
            decode(
                SetMethod(MAILBOX, "d1", destroy = listOf("#previous")),
                """{"newState":"s2","destroyed":["serverId"]}""",
            ).firstError,
        )
    }

    private fun decode(
        method: SetMethod,
        body: String,
    ): SetResult = jmapDecoding { method.decode(JmapJson.parseToJsonElement(body).jsonObject) }
}
