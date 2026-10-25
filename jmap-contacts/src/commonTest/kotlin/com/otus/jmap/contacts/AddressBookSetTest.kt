package com.otus.jmap.contacts

import com.otus.jmap.JmapException
import com.otus.jmap.JmapJson
import com.otus.jmap.SetError
import com.otus.jmap.SetResult
import com.otus.jmap.jmapDecoding
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddressBookSetTest {
    @Test
    fun createsABookAndMakesItTheDefaultByItsCreationId() =
        runTest {
            val server =
                ContactsServer {
                    """{"methodResponses": [["AddressBook/set", {"accountId": "a0x9", "oldState": "~4148",
                       "newState": "~4150", "created": {"k1": {"id": "b9", "isDefault": true,
                       "myRights": {"mayRead": true, "mayWrite": true, "mayShare": true, "mayDelete": true}}},
                       "updated": {"$AUTOSAVED": {"isDefault": false}}}, "c0"]], "sessionState": "s1"}"""
                }
            val create = mapOf("k1" to NewAddressBook("Work"))
            val method = AddressBookSetMethod("a0x9", create = create, onSuccessSetIsDefault = "#k1")
            val result = server.request { call(method) }
            assertEquals("b9", result.createdId("k1"))
            assertEquals(mapOf(AUTOSAVED to parseObject("""{"isDefault": false}""")), result.updated)
            assertEquals(
                parseObject(
                    """{"accountId": "a0x9", "create": {"k1": {"name": "Work", "isSubscribed": true}},
                    "onSuccessSetIsDefault": "#k1"}""",
                ),
                server.methodCalls().single().jsonArray[1],
            )
        }

    @Test
    fun destroyingABookWithItsCardsSendsOnDestroyRemoveContents() {
        val arguments =
            AddressBookSetMethod("a0x9", destroy = listOf("b1"), ifInState = "~4150", onDestroyRemoveContents = true)
                .arguments()
        assertEquals(
            parseObject(
                """{"accountId": "a0x9", "ifInState": "~4150", "destroy": ["b1"], "onDestroyRemoveContents": true}""",
            ),
            arguments,
        )
    }

    @Test
    fun argumentsWithTheirDefaultValuesAreLeftOut() {
        val update = mapOf("b1" to addressBookPatch { name("Family") })
        assertEquals(
            parseObject("""{"accountId": "a0x9", "update": {"b1": {"name": "Family"}}}"""),
            AddressBookSetMethod("a0x9", update = update).arguments(),
        )
    }

    @Test
    fun aBookThatStillHoldsCardsIsNotDestroyed() {
        val method = AddressBookSetMethod("a0x9", destroy = listOf("b1"))
        val result =
            decode(
                method,
                """{"accountId": "a0x9", "newState": "~4150",
                   "notDestroyed": {"b1": {"type": "addressBookHasContents"}}}""",
            )
        assertEquals(mapOf("b1" to SetError("addressBookHasContents")), result.notDestroyed)
    }

    @Test
    fun aDefaultChangeTheServerIgnoresIsNoError() {
        // RFC 9610, section 2.3: an unknown id or a refused change leaves the default as it was.
        val method = AddressBookSetMethod("a0x9", onSuccessSetIsDefault = "missing")
        val result = decode(method, """{"newState": "~4150"}""")
        assertTrue(result.updated.isEmpty())
        assertNull(result.firstError)
    }

    @Test
    fun newBooksSendTheirSharingWithAllFourRights() {
        val book =
            NewAddressBook(
                name = "Family",
                description = "Shared with the household",
                sortOrder = 3,
                isSubscribed = false,
                shareWith = mapOf("P2342fnddd20" to AddressBookRights(mayRead = true)),
            )
        assertEquals(
            parseObject(
                """{"name": "Family", "description": "Shared with the household", "sortOrder": 3,
                "isSubscribed": false, "shareWith": {"P2342fnddd20":
                {"mayRead": true, "mayWrite": false, "mayShare": false, "mayDelete": false}}}""",
            ),
            book.toJson(),
        )
    }

    @Test
    fun namesAndSortOrdersOutsideTheRfcLimitsFailBeforeSending() {
        // RFC 9610, section 2: 1 to 255 octets of UTF-8, and 0 <= sortOrder < 2^31.
        val longest = "é".repeat(127) + "a"
        assertEquals(longest, NewAddressBook(longest).name)
        assertEquals(2_147_483_647L, NewAddressBook("Work", sortOrder = 2_147_483_647L).sortOrder)
        listOf("", "é".repeat(128)).forEach { name ->
            assertFailsWith<IllegalArgumentException>(name) { NewAddressBook(name) }
        }
        listOf(-1L, 2_147_483_648L).forEach { sortOrder ->
            assertFailsWith<IllegalArgumentException>("$sortOrder") { NewAddressBook("Work", sortOrder = sortOrder) }
        }
    }

    @Test
    fun aResponseWithoutAnOutcomeForEachBookIsRejected() {
        val method = AddressBookSetMethod("a0x9", create = mapOf("k1" to NewAddressBook("Work")))
        assertFailsWith<JmapException.NotJmap> { decode(method, """{"newState": "~4150"}""") }
    }

    @Test
    fun extraArgumentsReachTheServerUnderTheirRfcNames() {
        val arguments =
            AddressBookSetMethod("a0x9", onDestroyRemoveContents = true, onSuccessSetIsDefault = "b1").arguments()
        assertEquals(JsonPrimitive(true), arguments["onDestroyRemoveContents"])
        assertEquals(JsonPrimitive("b1"), arguments["onSuccessSetIsDefault"])
    }

    private fun decode(
        method: AddressBookSetMethod,
        body: String,
    ): SetResult = jmapDecoding { method.decode(JmapJson.parseToJsonElement(body) as JsonObject) }
}
