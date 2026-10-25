package com.otus.accounts.presentation

import com.otus.accounts.domain.ConnectedAccount
import kotlin.test.Test
import kotlin.test.assertEquals

class OwnAddressesTest {
    @Test
    fun theSignInAndSendingAddressesAppearOnceWithoutWildcards() {
        val account =
            ConnectedAccount(
                "ada",
                "Ada",
                "ada@example.org",
                "mail.example.org",
                emailAddresses = listOf("Ada@example.org", "work@other.example", "*@example.org"),
            )
        assertEquals(listOf("ada@example.org", "work@other.example"), account.ownAddresses)
    }

    @Test
    fun aUsernameThatIsNoAddressIsLeftOut() {
        assertEquals(emptyList(), ConnectedAccount("ada", "Ada", "ada", "mail.example.org").ownAddresses)
    }
}
