package com.otusfoss.core.avatar

import com.otusfoss.core.jmap.JmapAuth
import com.otusfoss.core.jmap.JmapClient
import com.otusfoss.core.jmap.JmapConnection
import com.otusfoss.core.jmap.JmapJson
import com.otusfoss.core.jscontact.Card
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.content.TextContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ContactSearchTest {
    @Test
    fun findsTheExactAddressAfterThirtyTwoLabelMatches() =
        runTest {
            val fixture = SearchFixture()
            assertContentEquals(
                byteArrayOf(1),
                fixture.search(books = setOf("z-book", "book", "a-book"), email = " Target@Example.ORG ")?.bytes,
            )
            assertEquals(listOf(0L, 32L), fixture.positions)
            val expectedFilter =
                JmapJson.parseToJsonElement(
                    """{"operator":"AND","conditions":[{"email":" Target@Example.ORG "},
                    {"operator":"OR","conditions":[{"inAddressBook":"a-book"},
                    {"inAddressBook":"book"},{"inAddressBook":"z-book"}]}]}""",
                )
            assertEquals(listOf(expectedFilter, expectedFilter), fixture.filters)
        }

    @Test
    fun aSingleApprovedBookFiltersByItsCondition() =
        runTest {
            val fixture = SearchFixture(count = 1, matchAt = 0)
            assertContentEquals(byteArrayOf(1), fixture.search()?.bytes)
            val expectedFilter =
                JmapJson.parseToJsonElement(
                    """{"operator":"AND","conditions":[{"email":"target@example.org"},
                    {"inAddressBook":"book"}]}""",
                )
            assertEquals(listOf(expectedFilter), fixture.filters)
        }

    @Test
    fun noApprovedBooksRetainsAnEmptyOrAndRejectsReturnedCards() =
        runTest {
            val fixture = SearchFixture(count = 1, matchAt = 0)
            assertNull(fixture.search(books = emptySet()) { error("An unreadable card must not be loaded") })
            assertEquals(listOf(0L, 1L), fixture.positions)
            val expectedFilter =
                JmapJson.parseToJsonElement(
                    """{"operator":"AND","conditions":[{"email":"target@example.org"},
                    {"operator":"OR","conditions":[]}]}""",
                )
            assertEquals(listOf(expectedFilter, expectedFilter), fixture.filters)
        }

    @Test
    fun continuesWhenTheServerClampsPagesBelowTheRequestedSize() =
        runTest {
            val fixture = SearchFixture(pageSize = 2, count = 5, matchAt = 4)
            assertContentEquals(byteArrayOf(1), fixture.search()?.bytes)
            assertEquals(listOf(0L, 2L, 4L), fixture.positions)
        }

    @Test
    fun endsAtAnEmptyPageWhenNoExactMatchExists() =
        runTest {
            val fixture = SearchFixture(pageSize = 2, count = 3, matchAt = -1)
            assertNull(fixture.search())
            assertEquals(listOf(0L, 2L, 3L), fixture.positions)
        }

    @Test
    fun unreadableMatchesAndMissingImagesDoNotHideLaterPhotos() =
        runTest {
            val fixture = SearchFixture(pageSize = 1, count = 3, matchAt = 0)
            assertContentEquals(
                byteArrayOf(1),
                fixture
                    .search { card ->
                        if (card.id == "c2") AvatarImage(byteArrayOf(1)) else null
                    }?.bytes,
            )
            assertEquals(listOf(0L, 1L, 2L), fixture.positions)
        }

    @Test
    fun ignoresCardsOutsideTheApprovedBooksEvenIfTheServerReturnsThem() =
        runTest {
            val fixture = SearchFixture(count = 1, matchAt = 0, cardBook = "hidden")
            assertNull(fixture.search { error("An unreadable card must not be loaded") })
        }

    @Test
    fun changedQueriesAndNonAdvancingPagesStopTheSearch() =
        runTest {
            for (fixture in listOf(SearchFixture(changeState = true), SearchFixture(ignorePosition = true))) {
                assertNull(fixture.search())
                assertEquals(listOf(0L, 32L), fixture.positions)
            }
        }

    @Test
    fun slowSearchesReturnTheFallbackButParentCancellationPropagates() =
        runTest {
            val slow = SearchFixture(beforePage = { delay(20_000) })
            assertNull(slow.search())
            val cancelled = SearchFixture(beforePage = { throw CancellationException("cancelled") })
            assertFailsWith<CancellationException> { cancelled.search() }
        }
}

private class SearchFixture(
    private val pageSize: Int = 32,
    private val count: Int = 33,
    private val matchAt: Int = 32,
    private val cardBook: String = "book",
    private val changeState: Boolean = false,
    private val ignorePosition: Boolean = false,
    private val beforePage: suspend () -> Unit = {},
) {
    val positions = mutableListOf<Long>()
    val filters = mutableListOf<JsonElement>()
    private val connection =
        JmapConnection(
            JmapClient(
                MockEngine.create {
                    dispatcher = Dispatchers.Unconfined
                    addHandler { request ->
                        if (request.url.encodedPath == "/session") {
                            respond(SEARCH_SESSION)
                        } else {
                            beforePage()
                            val arguments =
                                JmapJson
                                    .parseToJsonElement((request.body as TextContent).text)
                                    .jsonObject
                                    .getValue("methodCalls")
                                    .jsonArray
                                    .first()
                                    .jsonArray[1]
                                    .jsonObject
                            filters += arguments.getValue("filter")
                            val position =
                                arguments
                                    .getValue("position")
                                    .jsonPrimitive.content
                                    .toLong()
                            assertEquals("32", arguments.getValue("limit").jsonPrimitive.content)
                            positions += position
                            respond(page(position))
                        }
                    }
                },
            ),
            "https://example.org/session",
            JmapAuth.Basic("ada", "secret"),
        )

    suspend fun search(
        books: Set<String> = setOf("book"),
        email: String = "target@example.org",
        load: suspend (Card) -> AvatarImage? = { AvatarImage(byteArrayOf(1)) },
    ): AvatarImage? = findContactAvatar(connection, "a", email, books, own = emptySet(), load)

    private fun page(position: Long): String {
        val start = if (ignorePosition) 0 else position.toInt()
        val indices = start until minOf(start + pageSize, count)
        val ids = indices.joinToString(",") { "\"c$it\"" }
        val cards =
            indices.joinToString(",") { index ->
                val address = if (matchAt >= 0 && index >= matchAt) "target@example.org" else "other$index@example.org"
                """{"id":"c$index","addressBookIds":{"$cardBook":true},
            "emails":{"e":{"address":"$address","label":"target@example.org"}}}"""
            }
        val state = if (changeState && position > 0) "q2" else "q1"
        return """{"methodResponses":[["ContactCard/query",{"accountId":"a","queryState":"$state",
            "canCalculateChanges":false,"position":$start,"ids":[$ids]},"c0"],
            ["ContactCard/get",{"accountId":"a","state":"c1","list":[$cards],"notFound":[]},"c1"]],
            "sessionState":"s1"}"""
    }
}

private const val SEARCH_SESSION = """
{"capabilities":{"urn:ietf:params:jmap:core":{},"urn:ietf:params:jmap:contacts":{}},
 "accounts":{"a":{"accountCapabilities":{"urn:ietf:params:jmap:contacts":{}}}},
 "apiUrl":"/api","state":"s1"}
"""
