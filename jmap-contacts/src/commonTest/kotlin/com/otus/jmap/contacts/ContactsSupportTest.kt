package com.otus.jmap.contacts

import com.otus.jmap.ChangesMethod
import com.otus.jmap.GetMethod
import com.otus.jmap.JmapCapability
import com.otus.jmap.JmapException
import com.otus.jmap.JmapMethod
import com.otus.jmap.QueryChangesMethod
import com.otus.jmap.QueryMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ContactsSupportTest {
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
            contactMethods("mail").forEach { method ->
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

    private fun contactMethods(accountId: String): List<JmapMethod<*>> =
        listOf(
            GetMethod(AddressBookType, accountId),
            ChangesMethod(AddressBookType, accountId, "~4144"),
            GetMethod(ContactCardType, accountId),
            ChangesMethod(ContactCardType, accountId, "s1"),
            QueryMethod(ContactCardType, accountId, ContactCardFilter(email = "joe").toFilter().toJson()),
            QueryChangesMethod(ContactCardType, accountId, "q1"),
        )
}
