package com.otusfoss.core.storage

import java.security.GeneralSecurityException
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KeystoreTextCipherTest {
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
        assertNull(cipher.open(SEALED_PREFIX + "not base64 !", "login/Email/1"))
        assertNull(cipher.open(SEALED_PREFIX + "AAAA", "login/Email/1"))
    }

    @Test
    fun theStorageKeyIsMadeOnceAndReadBackWrapped() {
        val preferences = Saved()
        val wrap = XorWrap()

        val first = storageKey(preferences.key, wrap)
        val second = storageKey(preferences.key, wrap)

        assertEquals(first.key.encoded.toList(), second.key.encoded.toList())
        assertEquals(32, first.key.encoded.size)
        assertFalse(first.key.encoded.joinToString(",") in preferences.value.orEmpty())
        assertEquals(KeyLife.First to KeyLife.Kept, first.life to second.life)
    }

    @Test
    fun aLostKeyIsReplacedForGood() {
        val preferences = Saved()
        val first = storageKey(preferences.key, XorWrap())

        val replaced = storageKey(preferences.key, XorWrap(unwrapped = Unwrapped.Lost, makesKey = false))

        assertNotEquals(first.key.encoded.toList(), replaced.key.encoded.toList())
        assertEquals(replaced.key.encoded.toList(), storageKey(preferences.key, XorWrap()).key.encoded.toList())
        assertEquals(KeyLife.Kept, replaced.life)
    }

    @Test
    fun aDeletedSavedKeyIsNotTakenForTheFirstStart() {
        val preferences = Saved()
        storageKey(preferences.key, XorWrap())
        preferences.value = null

        assertEquals(KeyLife.Kept, storageKey(preferences.key, XorWrap(makesKey = false)).life)
    }

    @Test
    fun aKeystoreThatFailsOnceLeavesTheSavedKeyAlone() {
        val preferences = Saved()
        val first = storageKey(preferences.key, XorWrap())

        val meanwhile = storageKey(preferences.key, XorWrap(unwrapped = Unwrapped.Failed))

        assertNotEquals(first.key.encoded.toList(), meanwhile.key.encoded.toList())
        assertEquals(first.key.encoded.toList(), storageKey(preferences.key, XorWrap()).key.encoded.toList())
        assertEquals(KeyLife.ProcessOnly, meanwhile.life)
    }

    @Test
    fun aKeystoreThatCannotWrapStillGivesAKeyForThisProcess() {
        val preferences = Saved()

        val key = storageKey(preferences.key, XorWrap(wraps = false))

        assertEquals(32, key.key.encoded.size)
        assertEquals(KeyLife.ProcessOnly, key.life)
        assertNull(preferences.value)
    }

    @Test
    fun aKeyThatCouldNotBeSavedLastsForThisProcessOnly() {
        val preferences = Saved().apply { saves = false }

        assertEquals(KeyLife.ProcessOnly, storageKey(preferences.key, XorWrap()).life)
    }

    private fun cipherWith(seed: Int) =
        KeystoreTextCipher { StorageKey(SecretKeySpec(ByteArray(32) { (it * seed).toByte() }, "AES"), KeyLife.Kept) }

    /** Stands in for the preferences that keep the wrapped key. */
    private class Saved {
        var value: String? = null
        var saves = true
        val key =
            WrappedKey({ value }) {
                if (saves) value = it
                saves
            }
    }

    /** Stands in for the Keystore: it hides the key's bytes, and fails the way the test asks. */
    private class XorWrap(
        private val unwrapped: Unwrapped? = null,
        private val wraps: Boolean = true,
        private val makesKey: Boolean = true,
    ) : KeyWrap {
        override fun wrap(key: ByteArray): Wrapped {
            if (!wraps) throw GeneralSecurityException("no keystore")
            return Wrapped(key.joinToString(",") { (it.toInt() xor MASK).toString() }, makesKey)
        }

        override fun unwrap(wrapped: String): Unwrapped =
            unwrapped ?: Unwrapped.Key(wrapped.split(",").map { (it.toInt() xor MASK).toByte() }.toByteArray())

        private companion object {
            const val MASK = 0x5a
        }
    }
}
