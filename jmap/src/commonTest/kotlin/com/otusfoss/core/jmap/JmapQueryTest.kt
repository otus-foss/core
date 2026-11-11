package com.otusfoss.core.jmap

import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class JmapQueryTest {
    @Test
    fun sendsTheRfcExampleQuery() {
        // The Todo/query request of RFC 8620, section 5.7; the client always writes isAscending.
        val method =
            QueryMethod(
                TODO_TYPE,
                "x",
                filter = anyOf(hasKeyword("music"), hasKeyword("video")),
                sort = listOf(JmapSort("title")),
                limit = 10,
                extra = mapOf("position" to JsonPrimitive(0)),
            )
        assertEquals(
            parseObject(
                """{"accountId": "x",
                "filter": {"operator": "OR", "conditions": [{"hasKeyword": "music"}, {"hasKeyword": "video"}]},
                "sort": [{"property": "title", "isAscending": true}], "position": 0, "limit": 10}""",
            ),
            method.arguments(),
        )
    }

    @Test
    fun readsTheRfcExampleResponse() {
        val result =
            decode(
                """{"accountId": "x", "queryState": "y13213", "canCalculateChanges": true, "position": 0,
                "ids": ["a", "b", "c", "d", "e", "f", "g", "h", "i", "j"]}""",
            )
        assertEquals(QueryResult("y13213", "abcdefghij".map { it.toString() }, 0, canCalculateChanges = true), result)
        assertNull(result.total)
        assertNull(result.limit)
        assertFalse(decode("""{"queryState": "q", "ids": []}""").canCalculateChanges)
    }

    @Test
    fun aCollationIsSentOnlyWhenChosen() {
        val sort = listOf(JmapSort("name"), JmapSort("name", isAscending = false, collation = "i;unicode-casemap"))
        assertEquals(
            JmapJson.parseToJsonElement(
                """[{"property": "name", "isAscending": true},
                {"property": "name", "isAscending": false, "collation": "i;unicode-casemap"}]""",
            ),
            sortJson(sort),
        )
    }

    @Test
    fun theServerLimitIsAnUnsignedInt() {
        assertEquals(256, decode("""{"queryState": "q", "ids": [], "limit": 256}""").limit)
        assertEquals(MAX_JMAP_UNSIGNED_INT, decode("""{"ids": [], "limit": 9007199254740991}""").limit)
        listOf("-1", "9007199254740992").forEach { limit ->
            assertFailsWith<JmapException.NotJmap>(limit) { decode("""{"ids": [], "limit": $limit}""") }
        }
    }

    private fun decode(body: String): QueryResult =
        jmapDecoding { QueryMethod(TODO_TYPE, "x").decode(parseObject(body)) }
}
