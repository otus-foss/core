package com.otus.jmap

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JmapQueryChangesTest {
    private val musicOrVideo = anyOf(hasKeyword("music"), hasKeyword("video"))

    @Test
    fun sendsTheFilterAndSortOfTheCachedQuery() {
        // The request of RFC 8620, section 5.7; the client always writes isAscending.
        val method = QueryChangesMethod(TODO_TYPE, "x", "y13213", musicOrVideo, listOf(JmapSort("title")), 50)
        assertEquals("Todo/queryChanges", method.name)
        assertEquals(
            parseObject(
                """{"accountId": "x",
                "filter": {"operator": "OR", "conditions": [{"hasKeyword": "music"}, {"hasKeyword": "video"}]},
                "sort": [{"property": "title", "isAscending": true}],
                "sinceQueryState": "y13213", "maxChanges": 50}""",
            ),
            method.arguments(),
        )
    }

    @Test
    fun typeSpecificArgumentsGoInExtra() {
        val extra = mapOf("upToId" to JsonPrimitive("j"), "calculateTotal" to JsonPrimitive(true))
        val arguments = QueryChangesMethod(TODO_TYPE, "x", "q1", extra = extra).arguments()
        assertEquals(
            parseObject("""{"accountId": "x", "sinceQueryState": "q1", "upToId": "j", "calculateTotal": true}"""),
            arguments,
        )
        listOf("accountId", "#filter", "sort", "sinceQueryState", "maxChanges").forEach { key ->
            assertFailsWith<IllegalArgumentException>(key) {
                QueryChangesMethod(TODO_TYPE, "x", "q1", extra = mapOf(key to JsonPrimitive("other")))
            }
        }
    }

    @Test
    fun readsTheRfcExampleAlongsideChanges() =
        runTest {
            // The responses of RFC 8620, section 5.7, which answer "added": null.
            val (engine, _) =
                fixtureServer(TODO_SESSION) {
                    """{"methodResponses": [["Todo/changes", {"accountId": "x", "oldState": "10324",
                       "newState": "871903", "hasMoreChanges": false, "created": [], "updated": [],
                       "destroyed": ["b"]}, "c0"],
                       ["Todo/queryChanges", {"accountId": "x", "oldQueryState": "y13213",
                       "newQueryState": "y13218", "removed": ["b"], "added": null}, "c1"]]}"""
                }
            val client = JmapClient(engine)
            lateinit var changes: JmapCall<ChangesResult>
            lateinit var queryChanges: JmapCall<QueryChangesResult>
            val response =
                client.send(client.session(TODO_SESSION_URL, ADA), ADA) {
                    changes = call(ChangesMethod(TODO_TYPE, "x", "10324", maxChanges = 50))
                    queryChanges =
                        call(QueryChangesMethod(TODO_TYPE, "x", "y13213", musicOrVideo, listOf(JmapSort("title")), 50))
                }
            assertEquals(listOf("b"), response[changes].destroyed)
            assertEquals(QueryChangesResult("y13213", "y13218", removed = listOf("b")), response[queryChanges])
        }

    @Test
    fun anOutdatedQueryStateIsAMethodError() =
        runTest {
            val (engine, _) =
                fixtureServer(TODO_SESSION) {
                    """{"methodResponses": [["error", {"type": "cannotCalculateChanges"}, "c0"]]}"""
                }
            val client = JmapClient(engine)
            val error =
                assertFailsWith<JmapException.MethodError> {
                    client.request(client.session(TODO_SESSION_URL, ADA), ADA) {
                        call(QueryChangesMethod(TODO_TYPE, "x", "old"))
                    }
                }
            assertEquals("cannotCalculateChanges", error.type)
        }

    @Test
    fun readsSplicesInIndexOrder() {
        // The splice example of RFC 8620, section 5.6, with a second added id.
        val result =
            decode(
                """{"oldQueryState": "q1", "newQueryState": "q2", "total": 9,
                "removed": ["id2", "id31"], "added": [{"id": "id5", "index": 0}, {"id": "id6", "index": 4}]}""",
            )
        assertEquals(listOf("id2", "id31"), result.removed)
        assertEquals(listOf(AddedItem("id5", 0), AddedItem("id6", 4)), result.added)
        assertEquals(9, result.total)
    }

    @Test
    fun malformedChangesAreJmapErrors() {
        listOf(
            """{"oldQueryState": "q1", "removed": []}""",
            """{"oldQueryState": "q1", "newQueryState": "q2", "total": -1}""",
            """{"oldQueryState": "q1", "newQueryState": "q2", "total": 9007199254740992}""",
            """{"oldQueryState": "q1", "newQueryState": "q2", "added": [{"id": "a", "index": -1}]}""",
            """{"oldQueryState": "q1", "newQueryState": "q2", "added": [{"id": "a"}]}""",
            """{"oldQueryState": "q1", "newQueryState": "q2",
            "added": [{"id": "a", "index": 4}, {"id": "b", "index": 0}]}""",
        ).forEach { body -> assertFailsWith<JmapException.NotJmap>(body) { decode(body) } }
    }

    private fun decode(body: String): QueryChangesResult =
        jmapDecoding { QueryChangesMethod(TODO_TYPE, "x", "q1").decode(parseObject(body)) }
}
