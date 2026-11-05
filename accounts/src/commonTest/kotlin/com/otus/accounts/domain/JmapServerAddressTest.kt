package com.otus.accounts.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class JmapServerAddressTest {
    @Test
    fun bareHostUsesTheWellKnownDiscoveryPath() {
        assertEquals(
            JmapServerAddress.Valid("https://mail.example.com/.well-known/jmap"),
            parseJmapServer("  Mail.Example.com  "),
        )
        assertEquals(
            JmapServerAddress.Valid("https://mail.example.com:8443/.well-known/jmap"),
            parseJmapServer("https://mail.example.com:8443/"),
        )
    }

    @Test
    fun explicitPathIsUsedAsTheSessionUrl() {
        assertEquals(
            JmapServerAddress.Valid("https://example.com/jmap/session"),
            parseJmapServer("https://example.com/jmap/session/"),
        )
    }

    @Test
    fun plainHttpAndMalformedInputAreRejected() {
        assertEquals(JmapServerAddress.Empty, parseJmapServer("   "))
        assertEquals(JmapServerAddress.Insecure, parseJmapServer("http://mail.example.com"))
        assertEquals(JmapServerAddress.Invalid, parseJmapServer("ftp://mail.example.com"))
        assertEquals(JmapServerAddress.Invalid, parseJmapServer("mail example.com"))
        assertEquals(JmapServerAddress.Invalid, parseJmapServer("https://"))
        assertEquals(JmapServerAddress.Invalid, parseJmapServer("-bad-.example.com"))
        assertEquals(JmapServerAddress.Invalid, parseJmapServer("asdf"))
        assertEquals(JmapServerAddress.Invalid, parseJmapServer("mail.example."))
        assertEquals(JmapServerAddress.Invalid, parseJmapServer("192.168.1.5"))
    }

    @Test
    fun aHostLongerThanAnyDnsNameIsRejectedWithoutOverflowingTheStack() {
        assertEquals(JmapServerAddress.Invalid, parseJmapServer("a.".repeat(5_000) + "example.com"))
        val longest = "${"a".repeat(63)}.".repeat(3) + "a".repeat(61)
        assertEquals(
            JmapServerAddress.Valid("https://$longest:8443/.well-known/jmap"),
            parseJmapServer("$longest:8443"),
        )
    }

    @Test
    fun draftNeverPrintsItsPassword() {
        assertFalse("hunter2" in JmapAccountDraft("https://a.example/.well-known/jmap", "me", "hunter2").toString())
    }
}
