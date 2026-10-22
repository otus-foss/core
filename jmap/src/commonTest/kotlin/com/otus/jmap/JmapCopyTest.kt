package com.otus.jmap

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class JmapCopyTest {
    @Test
    fun movesATodoToAnotherAccountAndKeepsTheImplicitDestroy() =
        runTest {
            // The request and both responses of the move in RFC 8620, section 5.7.
            val (engine, requests) =
                fixtureServer(TODO_SESSION) {
                    """{"methodResponses": [["Todo/copy", {"fromAccountId": "x", "accountId": "y",
                       "created": {"k5122": {"id": "DAf97"}}, "oldState": "c1d64ecb038c",
                       "newState": "33844835152b"}, "c0"],
                       ["Todo/set", {"accountId": "x", "oldState": "871903", "newState": "871909",
                       "destroyed": ["a"]}, "c0"]], "sessionState": "s1"}"""
                }
            val client = JmapClient(engine)
            val move = CopyMethod(TODO_TYPE, "x", "y", copies("k5122" to "a"), onSuccessDestroyOriginal = true)
            lateinit var copy: JmapCall<CopyResult>
            val response = client.send(client.session(TODO_SESSION_URL, ADA), ADA) { copy = call(move) }
            assertEquals("DAf97", response[copy].createdId("k5122"))
            assertEquals(
                CopyResult("x", "y", "c1d64ecb038c", "33844835152b", mapOf("k5122" to source("DAf97")), emptyMap()),
                response[copy],
            )
            val destroy = response.responses(copy).last()
            assertEquals("Todo/set", destroy.name)
            val destroyed = SetMethod(TODO_TYPE, "x", destroy = listOf("a")).decode(destroy.arguments)
            assertEquals(listOf("a"), destroyed.destroyed)
            assertEquals(
                parseObject(
                    """{"using": ["urn:ietf:params:jmap:core", "urn:example:todo"],
                    "methodCalls": [["Todo/copy", {"fromAccountId": "x", "accountId": "y",
                    "create": {"k5122": {"id": "a"}}, "onSuccessDestroyOriginal": true}, "c0"]]}""",
                ),
                parseObject(requests.last().body),
            )
        }

    @Test
    fun aFailedImplicitDestroyLeavesTheCopyReadable() =
        runTest {
            val (engine, _) =
                fixtureServer(TODO_SESSION) {
                    """{"methodResponses": [
                       ["Todo/copy", {"newState": "t2", "created": {"k1": {"id": "n1"}}}, "c0"],
                       ["error", {"type": "stateMismatch"}, "c0"]]}"""
                }
            val client = JmapClient(engine)
            val move = CopyMethod(TODO_TYPE, "x", "y", copies("k1" to "a"), onSuccessDestroyOriginal = true)
            lateinit var copy: JmapCall<CopyResult>
            val response = client.send(client.session(TODO_SESSION_URL, ADA), ADA) { copy = call(move) }
            assertEquals("n1", response[copy].createdId("k1"))
            assertEquals(listOf("Todo/copy", "error"), response.responses(copy).map { it.name })
        }

    @Test
    fun aCopyIsNotSentWhenEitherAccountLacksTheType() =
        runTest {
            val (engine, requests) = fixtureServer(TODO_SESSION) { error("nothing reaches the API") }
            val client = JmapClient(engine)
            val session = client.session(TODO_SESSION_URL, ADA)
            listOf("z" to "y", "x" to "z", "unknown" to "y").forEach { (from, to) ->
                val copy = CopyMethod(TODO_TYPE, from, to, copies("k1" to "a"))
                val error =
                    assertFailsWith<JmapException.Unsupported>("$from to $to") {
                        client.request(session, ADA) { call(copy) }
                    }
                assertEquals("urn:example:todo", error.capability)
            }
            assertEquals(1, requests.size, "Only the session was fetched")
        }

    @Test
    fun everyCopyGetsExactlyOneOutcomeAndCopiesHaveIds() {
        val method = CopyMethod(TODO_TYPE, "x", "y", copies("k1" to "a"))
        listOf(
            """{"created": {"k1": {"id": "n1"}}}""",
            """{"newState": "t2"}""",
            """{"newState": "t2", "created": {"k1": {}}}""",
            """{"newState": "t2", "created": {"k1": {"id": 7}}}""",
            """{"newState": "t2", "created": {"k1": {"id": "n1"}}, "notCreated": {"k1": {"type": "forbidden"}}}""",
        ).forEach { body -> assertFailsWith<JmapException.NotJmap>(body) { decode(method, body) } }
        val rejected =
            decode(
                method,
                """{"newState": "t2", "notCreated": {"k1": {"type": "alreadyExists", "existingId": "n0"}}}""",
            )
        assertNull(rejected.createdId("k1"))
        assertNull(rejected.oldState)
        assertEquals(SetError("alreadyExists", existingId = "n0"), rejected.notCreated.getValue("k1"))
    }

    @Test
    fun aCopyNamesTwoAccountsAndEachSourceId() {
        assertFailsWith<IllegalArgumentException> { CopyMethod(TODO_TYPE, "x", "x", copies("k1" to "a")) }
        val withoutSource = mapOf("k1" to JsonObject(emptyMap()))
        assertFailsWith<IllegalArgumentException> { CopyMethod(TODO_TYPE, "x", "y", withoutSource) }
        val renamed =
            buildJsonObject {
                put("id", JsonPrimitive("a"))
                put("title", JsonPrimitive("Practise Piano"))
            }
        val arguments =
            CopyMethod(TODO_TYPE, "x", "y", mapOf("k1" to renamed), ifFromInState = "f1", ifInState = "t1").arguments()
        assertEquals(
            parseObject(
                """{"fromAccountId": "x", "ifFromInState": "f1", "accountId": "y", "ifInState": "t1",
                "create": {"k1": {"id": "a", "title": "Practise Piano"}}}""",
            ),
            arguments,
        )
        assertFalse("onSuccessDestroyOriginal" in arguments)
    }

    /** Copy requests keyed by creation id, each naming only its source id. */
    private fun copies(vararg sources: Pair<String, String>): Map<String, JsonObject> =
        sources.associate { (creationId, id) -> creationId to source(id) }

    private fun source(id: String): JsonObject = buildJsonObject { put("id", JsonPrimitive(id)) }

    private fun decode(
        method: CopyMethod,
        body: String,
    ): CopyResult = jmapDecoding { method.decode(parseObject(body)) }
}
