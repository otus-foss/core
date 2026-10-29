package com.otus.jmap.contacts

import com.otus.jmap.ChangesMethod
import com.otus.jmap.CopyMethod
import com.otus.jmap.GetMethod
import com.otus.jmap.JmapAccount
import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapException
import com.otus.jmap.JmapMethod
import com.otus.jmap.QueryChangesMethod
import com.otus.jmap.QueryMethod
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ContactsSupportTest {
    @Test
    fun eachAccountReportsItsContactsCapability() =
        runTest {
            val accounts = ContactsServer().session().accounts
            assertEquals(ContactsCapability(mayCreateAddressBook = true), accounts["a0x9"]?.contacts)
            assertEquals(ContactsCapability(maxAddressBooksPerCard = 1), accounts["team"]?.contacts)
            assertNull(accounts["mail"]?.contacts)
        }

    @Test
    fun aMalformedCapabilityReadsAsTheCautiousDefaults() {
        // RFC 9610, section 1.4.1: support is the key's presence, so a bad value still means contacts.
        listOf(
            """{"maxAddressBooksPerCard": 0, "mayCreateAddressBook": true}""",
            """{"maxAddressBooksPerCard": "many", "mayCreateAddressBook": true}""",
            """{"mayCreateAddressBook": "yes"}""",
        ).forEach { value ->
            assertEquals(ContactsCapability(), account(parseObject(value)).contacts, value)
        }
        val withoutObject = JmapAccount("Ada", isPersonal = true, isReadOnly = false, setOf(JmapCapability.CONTACTS))
        assertEquals(ContactsCapability(), withoutObject.contacts)
    }

    @Test
    fun contactMethodsAreRefusedWhenTheServerLacksContacts() =
        runTest {
            val server = ContactsServer(session = contactsSession(contacts = false))
            contactMethods("a0x9").forEach { method ->
                val error = assertFailsWith<JmapException.Unsupported>(method.name) { server.request { call(method) } }
                assertEquals(JmapCapability.CONTACTS, error.capability)
            }
            assertEquals(emptyList(), server.bodies, "Nothing reaches the API")
        }

    @Test
    fun contactMethodsAreRefusedForAnAccountWithoutContacts() =
        runTest {
            val server = ContactsServer()
            val copies = mapOf("k1" to buildJsonObject { put("id", JsonPrimitive("c1")) })
            val methods =
                contactMethods("mail") +
                    CopyMethod(ContactCardType, "mail", "a0x9", copies) +
                    CopyMethod(ContactCardType, "a0x9", "mail", copies)
            methods.forEach { method ->
                val error = assertFailsWith<JmapException.Unsupported>(method.name) { server.request { call(method) } }
                assertEquals(JmapCapability.CONTACTS, error.capability)
            }
            assertEquals(emptyList(), server.bodies, "Nothing reaches the API")
        }

    @Test
    fun addressBooksDisplayBySortOrderThenName() {
        val books =
            listOf(
                AddressBook("b4", name = "zeta", sortOrder = 0),
                AddressBook("b3", name = "Work", sortOrder = 1),
                AddressBook("b2", name = "Alpha", sortOrder = 0),
                AddressBook("b1", name = "alpha", sortOrder = 0),
                AddressBook("b0", name = "Alpha", sortOrder = 0),
            )
        assertEquals(listOf("b0", "b2", "b1", "b4", "b3"), books.sortedWith(AddressBookOrder).map { it.id })
    }

    private fun account(capability: JsonObject) =
        JmapAccount(
            name = "Ada",
            isPersonal = true,
            isReadOnly = false,
            capabilities = setOf(JmapCapability.CONTACTS),
            capabilityObjects = mapOf(JmapCapability.CONTACTS to capability),
        )

    private fun contactMethods(accountId: String): List<JmapMethod<*>> =
        listOf(
            GetMethod(AddressBookType, accountId),
            ChangesMethod(AddressBookType, accountId, "~4144"),
            AddressBookSetMethod(accountId, onSuccessSetIsDefault = AUTOSAVED),
            GetMethod(ContactCardType, accountId),
            ChangesMethod(ContactCardType, accountId, "s1"),
            QueryMethod(ContactCardType, accountId, ContactCardFilter(email = "joe").toFilter().toJson()),
            QueryChangesMethod(ContactCardType, accountId, "q1"),
        )
}
