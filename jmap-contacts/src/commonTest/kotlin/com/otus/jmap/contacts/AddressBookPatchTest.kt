package com.otus.jmap.contacts

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AddressBookPatchTest {
    @Test
    fun eachPropertyIsPatchedByItsRfcName() {
        val patch =
            addressBookPatch {
                name("Family")
                description("Shared with the household")
                sortOrder(4)
                isSubscribed(false)
            }
        assertEquals(
            parseObject(
                """{"name": "Family", "description": "Shared with the household", "sortOrder": 4,
                "isSubscribed": false}""",
            ),
            patch,
        )
    }

    @Test
    fun aNullDescriptionRemovesIt() {
        assertEquals(parseObject("""{"description": null}"""), addressBookPatch { description(null) })
    }

    @Test
    fun sharingChangesOnePrincipalAndLeavesTheOthers() {
        // RFC 9670, section 4: per-Principal paths keep every sharee the patch does not name.
        val patch =
            addressBookPatch {
                share("P2342fnddd20", AddressBookRights(mayRead = true, mayWrite = true))
                unshare("P674pp24095qo49pr")
                grant("Pa1", AddressBookRights::mayWrite)
                revoke("Pa1", AddressBookRights::mayShare)
            }
        assertEquals(
            parseObject(
                """{"shareWith/P2342fnddd20":
                {"mayRead": true, "mayWrite": true, "mayShare": false, "mayDelete": false},
                "shareWith/P674pp24095qo49pr": null,
                "shareWith/Pa1/mayWrite": true,
                "shareWith/Pa1/mayShare": false}""",
            ),
            patch,
        )
    }

    @Test
    fun theWholeMapReplacesEverySharee() {
        // RFC 9670, Figure 4 replaces the whole map; AddressBook rights take the place of TodoList's.
        val shared = mapOf("P2342fnddd20" to AddressBookRights(mayRead = true, mayWrite = true))
        assertEquals(
            parseObject(
                """{"shareWith": {"P2342fnddd20":
                {"mayRead": true, "mayWrite": true, "mayShare": false, "mayDelete": false}}}""",
            ),
            addressBookPatch { shareWith(shared) },
        )
        assertEquals(parseObject("""{"shareWith": null}"""), addressBookPatch { shareWith(null) })
    }

    @Test
    fun aPatchCannotReplaceTheMapAndChangeAnEntryInIt() {
        // RFC 8620, section 5.3: one path may not be a prefix of another in the same patch.
        assertFailsWith<IllegalArgumentException> {
            addressBookPatch {
                shareWith(emptyMap())
                share("P2342fnddd20", AddressBookRights(mayRead = true))
            }
        }
    }

    @Test
    fun invalidNamesAndSortOrdersFailBeforeSending() {
        assertFailsWith<IllegalArgumentException> { addressBookPatch { name("") } }
        assertFailsWith<IllegalArgumentException> { addressBookPatch { name("x".repeat(256)) } }
        assertFailsWith<IllegalArgumentException> { addressBookPatch { sortOrder(-1) } }
        assertFailsWith<IllegalArgumentException> { addressBookPatch { sortOrder(2_147_483_648L) } }
    }

    @Test
    fun principalIdsAreEscapedAsJsonPointerSegments() {
        // Ids never need escaping (RFC 8620, section 1.2), but a server that breaks that rule still gets a valid path.
        assertEquals(parseObject("""{"shareWith/a~1b~0c": null}"""), addressBookPatch { unshare("a/b~c") })
    }
}
