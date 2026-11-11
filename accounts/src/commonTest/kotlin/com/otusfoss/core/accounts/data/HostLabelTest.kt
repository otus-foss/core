package com.otusfoss.core.accounts.data

import kotlin.test.Test
import kotlin.test.assertEquals

class HostLabelTest {
    @Test
    fun defaultPortsShowTheHostAlone() {
        assertEquals("login.example.net", hostLabel("login.example.net", "https://login.example.net:443"))
        assertEquals("login.example.net", hostLabel("login.example.net", "http://login.example.net:80"))
    }

    @Test
    fun otherPortsAreShownBeforeTheyAreTrusted() {
        assertEquals("mail.example.com:8443", hostLabel("mail.example.com", "https://mail.example.com:8443"))
        assertEquals("mail.example.com:443", hostLabel("mail.example.com", "http://mail.example.com:443"))
    }
}
