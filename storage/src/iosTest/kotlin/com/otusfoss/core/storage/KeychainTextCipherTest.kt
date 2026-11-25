package com.otusfoss.core.storage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KeychainTextCipherTest {
    private val cipher = cipherWith(1)

    @Test
    fun sealedTextOpensAgainAndHidesItsText() {
        val text = "Subject: Hello ✉ \n" + "body ".repeat(2_000)

        val sealed = cipher.seal(text, "login/Email/1")

        assertTrue(isSealedText(sealed))
        assertFalse("Hello" in sealed)
        assertEquals(text, cipher.open(sealed, "login/Email/1"))
        assertEquals("", cipher.open(cipher.seal("")))
    }

    @Test
    fun theSameTextSealsDifferentlyEachTime() {
        assertNotEquals(cipher.seal("same", "a"), cipher.seal("same", "a"))
    }

    @Test
    fun anotherContextKeyOrAChangedTextDoesNotOpen() {
        val sealed = cipher.seal("secret", "login/Email/1")

        assertNull(cipher.open(sealed, "login/Email/2"))
        assertNull(cipherWith(2).open(sealed, "login/Email/1"))
        assertNull(cipher.open(sealed.dropLast(4) + "AAAA", "login/Email/1"))
        assertNull(cipher.open("plain text", "login/Email/1"))
        assertNull(cipher.open(SEALED_PREFIX + "AAAA", "login/Email/1"))
    }

    private fun cipherWith(seed: Int) = KeychainTextCipher { ByteArray(64) { (it * seed).toByte() } }
}
