package com.otusfoss.core.avatar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PublicAvatarHostTest {
    @Test
    fun publicNamesAreAllowedEvenWhenALabelLooksPrivate() {
        listOf(
            "proton.me",
            "EMAIL.OPENAI.COM",
            "company.co.uk",
            "site.xn--p1ai",
            "plan.com",
            "lan.com",
            "home.example.com",
            "localhost.net",
            "my-site.io",
            "123.com",
        ).forEach { assertTrue(isPublicAvatarHost(it), it) }
    }

    @Test
    fun singleLabelSpecialUseAndPrivateNetworkNamesAreRefused() {
        listOf(
            "printer",
            "localhost",
            "lan",
            "printer.lan",
            "ROUTER.LAN",
            "nas.home.arpa",
            "home.arpa",
            "intranet.corp",
            "media.home",
            "wiki.intranet",
            "files.private",
            "host.localdomain",
            "printer.local",
            "db.internal",
            "a.test",
            "a.invalid",
            "company.example",
            "abc.onion",
            "name.alt",
            "app.localhost",
        ).forEach { assertFalse(isPublicAvatarHost(it), it) }
    }

    @Test
    fun ipLiteralsInEveryFormTheUrlParserAcceptsAreRefused() {
        listOf(
            "127.0.0.1",
            "192.168.1.1",
            "127.1",
            "2130706433",
            "0x7f000001",
            "0x7f.0x1",
            "0177.0.0.1",
            "[::1]",
            "[::ffff:10.0.0.1]",
            "[fe80::1%25eth0]",
            "::1",
        ).forEach { assertFalse(isPublicAvatarHost(it), it) }
    }

    @Test
    fun absoluteEncodedAndNonAsciiNamesCannotBypassTheCheck() {
        listOf(
            "printer.lan.",
            "proton.me.",
            "printer%2elan",
            "printer.\uFF4C\uFF41\uFF4E", // fullwidth "lan"
            "\u212Aompany.com", // Kelvin sign, which lowercases to an ASCII "k"
            "printer..lan",
            "-printer.com",
            "printer-.com",
            "printer_1.com",
            "user@printer.com",
            "printer.com:443",
            "",
        ).forEach { assertFalse(isPublicAvatarHost(it), it) }
    }

    @Test
    fun senderDomainsUnderPrivateNamesKeepTheirInitials() {
        listOf("ada@printer.lan", "ada@nas.home.arpa", "ada@0x7f.0x1", "ada@\u212Aompany.com")
            .forEach { assertNull(avatarIconDomain(it), it) }
        assertEquals("company.com", avatarIconDomain("ada@Company.COM"))
    }

    @Test
    fun publicUrlsUseHttpsOnTheDefaultPortAtAPublicHostWithoutCredentials() {
        assertTrue(isPublicAvatarUrl("https://mail.example.net/api/favicon?domain=shop.com"))
        assertTrue(isPublicAvatarUrl("https://mail.example.net:443/"))
        listOf(
            "http://mail.example.net/",
            "https://mail.example.net:8443/",
            "https://ada@mail.example.net/",
            "https://ada:secret@mail.example.net/",
            "https://nas.lan/",
            "https://192.168.1.2/",
            "https://localhost/",
            "not a url",
        ).forEach { assertFalse(isPublicAvatarUrl(it), it) }
    }
}
