package com.otus.jmap.contacts

import com.otus.jmap.GetMethod
import com.otus.jmap.GetResult
import com.otus.jmap.JmapCall
import com.otus.jmap.JmapException
import com.otus.jscontact.Card
import com.otus.jscontact.ContactContext
import com.otus.jscontact.EmailAddress
import com.otus.jscontact.NameComponent
import com.otus.jscontact.NameComponentKind
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The examples of RFC 9610, section 4, sent and read through this module. */
class ContactsExamplesTest {
    @Test
    fun fetchingInitialDataSendsFigure1AndReadsFigure2() =
        runTest {
            val server = ContactsServer { responseOf(FIGURE_2) }
            lateinit var books: JmapCall<GetResult<AddressBook>>
            lateinit var cards: JmapCall<GetResult<Card>>
            val response =
                server.send {
                    books = call(GetMethod(AddressBookType, "a0x9"))
                    cards = call(GetMethod(ContactCardType, "a0x9"))
                }
            assertEquals(numbered(FIGURE_1), normalized(server.methodCalls()))
            val using = """"using":["urn:ietf:params:jmap:core","urn:ietf:params:jmap:contacts"]"""
            assertTrue(using in server.bodies.last(), server.bodies.last())
            assertEquals(GetResult("~4144", listOf(personal, autosaved), emptyList()), response[books])
            assertEquals("ewarbckaqJ::112", response[cards].state)
            val card = response[cards].list.single()
            assertEquals("3", card.id)
            assertEquals(mapOf(PERSONAL to true), card.addressBookIds)
            assertEquals(
                listOf(
                    NameComponent("Joe", NameComponentKind.Given),
                    NameComponent("Bloggs", NameComponentKind.Surname),
                ),
                card.name?.components,
            )
            assertEquals(true, card.name?.isOrdered)
            assertEquals(
                mapOf("0" to EmailAddress("joe.bloggs@example.com", contexts = mapOf(ContactContext.Private to true))),
                card.emails,
            )
            // The example card lacks the uid that JSContact makes mandatory, as a partial fetch would.
            assertNull(card.uid)
            assertTrue(card.validate().any { it.path == "/uid" }, "${card.validate()}")
        }

    @Test
    fun theRfcBooksDisplayPersonalFirst() {
        assertEquals(listOf(personal, autosaved), listOf(autosaved, personal).sortedWith(AddressBookOrder))
    }

    @Test
    fun changingTheDefaultSendsFigure3() =
        runTest {
            val server = ContactsServer { responseOf(FIGURE_4_CORRECTED) }
            val result = server.request { call(AddressBookSetMethod("a0x9", onSuccessSetIsDefault = AUTOSAVED)) }
            assertEquals(numbered(FIGURE_3), normalized(server.methodCalls()))
            assertEquals("~4144", result.oldState)
            assertEquals("~4148", result.newState)
            assertEquals(
                mapOf(
                    AUTOSAVED to parseObject("""{"isDefault": true}"""),
                    PERSONAL to parseObject("""{"isDefault": false}"""),
                ),
                result.updated,
            )
        }

    @Test
    fun figure4NestsItsStatesInUpdatedAndIsRejected() =
        runTest {
            // Figure 4 puts oldState and newState inside updated and leaves out the required newState.
            // Reading "~4144" as an updated AddressBook would be wrong, so the response is not JMAP.
            val server = ContactsServer { responseOf(FIGURE_4) }
            assertFailsWith<JmapException.NotJmap> {
                server.request { call(AddressBookSetMethod("a0x9", onSuccessSetIsDefault = AUTOSAVED)) }
            }
        }

    private val personal =
        AddressBook(
            id = PERSONAL,
            name = "Personal",
            sortOrder = 0,
            isDefault = true,
            isSubscribed = true,
            shareWith = mapOf("3f1502e0-63fe-4335-9ff3-e739c188f5dd" to AddressBookRights(mayRead = true)),
            myRights = AddressBookRights(mayRead = true, mayWrite = true, mayShare = true),
        )

    private val autosaved =
        AddressBook(
            id = AUTOSAVED,
            name = "Autosaved",
            sortOrder = 1,
            isSubscribed = true,
            myRights = AddressBookRights(mayRead = true, mayWrite = true, mayShare = true),
        )
}
