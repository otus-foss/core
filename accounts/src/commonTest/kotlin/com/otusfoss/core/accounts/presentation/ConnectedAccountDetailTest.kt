package com.otusfoss.core.accounts.presentation

import com.otusfoss.core.accounts.domain.ConnectedAccount
import com.otusfoss.core.accounts.presentation.components.connectedAccountDetail
import kotlin.test.Test
import kotlin.test.assertEquals

class ConnectedAccountDetailTest {
    @Test
    fun anAddressUsernameMovesToItsPill() {
        val account = ConnectedAccount("ada", "Ada Lovelace", "ada@example.org", "mail.example.org")
        assertEquals("mail.example.org", connectedAccountDetail(account))
    }

    @Test
    fun aUsernameThatIsNoAddressStaysInTheDetail() {
        val account = ConnectedAccount("ada", "Ada Lovelace", "ada", "mail.example.org")
        assertEquals("ada\nmail.example.org", connectedAccountDetail(account))
    }

    @Test
    fun anAccountNamedByItsAddressShowsOnlyTheServer() {
        val account = ConnectedAccount("otus", "otus@rubylabs.design", "Otus@RubyLabs.design", "mail.hilfikernet.ch")
        assertEquals("mail.hilfikernet.ch", connectedAccountDetail(account))
    }

    @Test
    fun onlyWildcardAddressesStayInTheDetail() {
        val account =
            ConnectedAccount(
                "ada",
                "Ada",
                "ada@example.org",
                "mail.example.org",
                emailAddresses = listOf("Ada@example.org", "work@other.example", "work@other.example", "*@example.org"),
            )
        assertEquals("*@example.org\nmail.example.org", connectedAccountDetail(account))
    }

    @Test
    fun addressesWithPillsLeaveOnlyTheServer() {
        val account =
            ConnectedAccount(
                "ada",
                "ada@example.org",
                "Ada@example.org",
                "mail.example.org",
                emailAddresses = listOf("ada@example.org", "ada@other.example"),
            )
        assertEquals("mail.example.org", connectedAccountDetail(account))
    }
}
