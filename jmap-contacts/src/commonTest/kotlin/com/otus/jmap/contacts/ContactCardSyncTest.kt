package com.otus.jmap.contacts

import com.otus.jmap.AddedItem
import com.otus.jmap.CopyMethod
import com.otus.jmap.CopyResult
import com.otus.jmap.GetMethod
import com.otus.jmap.GetResult
import com.otus.jmap.JmapCall
import com.otus.jmap.JmapIds
import com.otus.jmap.QueryChangesMethod
import com.otus.jmap.QueryChangesResult
import com.otus.jmap.QueryMethod
import com.otus.jmap.QueryResult
import com.otus.jmap.SetError
import com.otus.jmap.SetMethod
import com.otus.jscontact.Card
import com.otus.jscontact.EmailAddress
import com.otus.jscontact.Name
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/** ContactCard through the generic RFC 8620 methods (RFC 9610, sections 3.1 to 3.6). */
class ContactCardSyncTest {
    @Test
    fun queryChangesUpdatesASortedFilteredList() =
        runTest {
            val server =
                ContactsServer {
                    """{"methodResponses": [["ContactCard/queryChanges", {"accountId": "a0x9",
                       "oldQueryState": "q1", "newQueryState": "q2", "total": 3, "removed": ["c2"],
                       "added": [{"id": "c5", "index": 0}]}, "c0"]]}"""
                }
            val filter = ContactCardFilter(inAddressBook = PERSONAL).toFilter().toJson()
            val sort = listOf(ContactCardSort.nameSurname())
            val method =
                QueryChangesMethod(
                    ContactCardType,
                    "a0x9",
                    "q1",
                    filter,
                    sort,
                    maxChanges = 50,
                    extra = mapOf("calculateTotal" to JsonPrimitive(true)),
                )
            val result = server.request { call(method) }
            assertEquals(QueryChangesResult("q1", "q2", 3, listOf("c2"), listOf(AddedItem("c5", 0))), result)
            assertEquals(
                parseObject(
                    """{"accountId": "a0x9", "filter": {"inAddressBook": "$PERSONAL"},
                    "sort": [{"property": "name/surname", "isAscending": true}], "sinceQueryState": "q1",
                    "maxChanges": 50, "calculateTotal": true}""",
                ),
                server.methodCalls().single().jsonArray[1],
            )
            assertEquals(JsonPrimitive("ContactCard/queryChanges"), server.methodCalls().single().jsonArray[0])
        }

    @Test
    fun copyMovesACardFromASharedAccount() =
        runTest {
            val server =
                ContactsServer {
                    """{"methodResponses": [
                       ["ContactCard/copy", {"fromAccountId": "team", "accountId": "a0x9", "oldState": "s1",
                       "newState": "s2", "created": {"k1": {"id": "c9"}},
                       "notCreated": {"k2": {"type": "alreadyExists", "existingId": "c3"}}}, "c0"],
                       ["ContactCard/set", {"accountId": "team", "oldState": "t1", "newState": "t2",
                       "destroyed": ["c7"]}, "c0"]]}"""
                }
            val create =
                mapOf(
                    "k1" to copyOf("c7"),
                    // The target account already holds a card with this one's uid (RFC 9610, section 3).
                    "k2" to copyOf("c8"),
                )
            lateinit var copy: JmapCall<CopyResult>
            val move = CopyMethod(ContactCardType, "team", "a0x9", create, onSuccessDestroyOriginal = true)
            val response = server.send { copy = call(move) }
            assertEquals("c9", response[copy].createdId("k1"))
            assertEquals(SetError("alreadyExists", existingId = "c3"), response[copy].notCreated["k2"])
            val destroy = response.responses(copy).last()
            val destroyed = SetMethod(ContactCardType, "team", destroy = listOf("c7")).decode(destroy.arguments)
            assertEquals(listOf("c7"), destroyed.destroyed)
            assertEquals(
                parseObject(
                    """{"fromAccountId": "team", "accountId": "a0x9", "onSuccessDestroyOriginal": true,
                    "create": {"k1": {"id": "c7", "addressBookIds": {"$PERSONAL": true}},
                    "k2": {"id": "c8", "addressBookIds": {"$PERSONAL": true}}}}""",
                ),
                server.methodCalls().single().jsonArray[1],
            )
        }

    @Test
    fun queryAndGetReadPartialCards() =
        runTest {
            val server =
                ContactsServer {
                    """{"methodResponses": [
                       ["ContactCard/query", {"accountId": "a0x9", "queryState": "q1", "canCalculateChanges": true,
                       "position": 0, "ids": ["c1"]}, "c0"],
                       ["ContactCard/get", {"accountId": "a0x9", "state": "s1", "notFound": [],
                       "list": [{"id": "c1", "name": {"full": "Joe Bloggs"},
                       "emails": {"e1": {"address": "joe@example.com"}}}]}, "c1"]]}"""
                }
            lateinit var query: JmapCall<QueryResult>
            lateinit var get: JmapCall<GetResult<Card>>
            val filter = ContactCardFilter(email = "joe").toFilter().toJson()
            val response =
                server.send {
                    query = call(QueryMethod(ContactCardType, "a0x9", filter, listOf(ContactCardSort.created()), 10))
                    val ids = JmapIds.Result(query.resultOf("/ids"))
                    get = call(GetMethod(ContactCardType, "a0x9", ids, listOf("id", "name", "emails")))
                }
            assertEquals(true, response[query].canCalculateChanges)
            val card = response[get].list.single()
            assertEquals(Name(full = "Joe Bloggs"), card.name)
            assertEquals(mapOf("e1" to EmailAddress("joe@example.com")), card.emails)
            assertNull(card.uid)
        }

    @Test
    fun setCreatesFromACardAndUpdatesByPatch() =
        runTest {
            val server =
                ContactsServer {
                    """{"methodResponses": [["ContactCard/set", {"accountId": "a0x9", "oldState": "s1",
                       "newState": "s2", "created": {"k1": {"id": "c9"}}, "updated": {"c1": null}}, "c0"]]}"""
                }
            val card = Card(uid = "urn:uuid:4f5e", name = Name(full = "Ada"), addressBookIds = mapOf(PERSONAL to true))
            val create = mapOf("k1" to card.toCreation())
            val update = mapOf("c1" to contactCardPatch { addressBookIds.add(AUTOSAVED) })
            val result = server.request { call(SetMethod(ContactCardType, "a0x9", create, update)) }
            assertEquals("c9", result.createdId("k1"))
            assertEquals(
                parseObject(
                    """{"accountId": "a0x9", "create": {"k1": {"@type": "Card", "version": "1.0",
                    "uid": "urn:uuid:4f5e", "name": {"@type": "Name", "full": "Ada"},
                    "addressBookIds": {"$PERSONAL": true}}}, "update": {"c1": {"addressBookIds/$AUTOSAVED": true}}}""",
                ),
                server.methodCalls().single().jsonArray[1],
            )
        }

    @Test
    fun aCreationNeedsAnAddressBook() {
        listOf(
            Card(uid = "u1"),
            Card(uid = "u1", addressBookIds = emptyMap()),
            Card(uid = "u1", addressBookIds = mapOf("b1" to false)),
        ).forEach { card -> assertFailsWith<IllegalArgumentException>("$card") { card.toCreation() } }
    }

    private fun copyOf(id: String) =
        buildJsonObject {
            put("id", JsonPrimitive(id))
            put("addressBookIds", buildJsonObject { put(PERSONAL, JsonPrimitive(true)) })
        }
}
