package com.otus.accounts.presentation

import com.otus.accounts.domain.ConnectedAccount
import com.otus.accounts.presentation.components.connectedAccountDetail
import kotlin.test.Test
import kotlin.test.assertEquals

class ConnectedAccountDetailTest {
    @Test
    fun aDisplayNameKeepsTheUsernameBelowIt() {
        val account = ConnectedAccount("ada", "Ada Lovelace", "ada@example.org", "mail.example.org")
        assertEquals("ada@example.org\nmail.example.org", connectedAccountDetail(account))
    }

    @Test
    fun anAccountNamedByItsAddressShowsOnlyTheServer() {
        val account = ConnectedAccount("otus", "otus@rubylabs.design", "Otus@RubyLabs.design", "mail.hilfikernet.ch")
        assertEquals("mail.hilfikernet.ch", connectedAccountDetail(account))
    }

    @Test
    fun multipleAddressesAppearOnceBelowTheDisplayName() {
        val account =
            ConnectedAccount(
                "ada",
                "Ada",
                "ada@example.org",
                "mail.example.org",
                emailAddresses = listOf("Ada@example.org", "work@other.example", "work@other.example", "*@example.org"),
            )
        assertEquals(
            "ada@example.org\nwork@other.example\n*@example.org\nmail.example.org",
            connectedAccountDetail(account),
        )
    }

    @Test
    fun theAccountNameDoesNotRepeatInItsAddressList() {
        val account =
            ConnectedAccount(
                "ada",
                "ada@example.org",
                "Ada@example.org",
                "mail.example.org",
                emailAddresses = listOf("ada@example.org", "ada@other.example"),
            )
        assertEquals("ada@other.example\nmail.example.org", connectedAccountDetail(account))
    }
}
