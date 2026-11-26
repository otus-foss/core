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
        val wrap = XorWrap()
        val first = storageKey(preferences.key, wrap)
        wrap.unwrapped = Unwrapped.Lost

        val replaced = storageKey(preferences.key, wrap)
        wrap.unwrapped = null

        assertNotEquals(first.key.encoded.toList(), replaced.key.encoded.toList())
        assertEquals(replaced.key.encoded.toList(), storageKey(preferences.key, wrap).key.encoded.toList())
        assertEquals(KeyLife.Kept, replaced.life)
    }

    @Test
    fun anInvalidatedKeystoreKeyIsDeletedAndReplacedWithoutFirstLife() {
        val preferences = Saved()
        val wrap = XorWrap()
        val first = storageKey(preferences.key, wrap)
        wrap.invalidated = true

        val replaced = storageKey(preferences.key, wrap)

        assertNotEquals(first.key.encoded.toList(), replaced.key.encoded.toList())
        assertEquals(KeyLife.Kept, replaced.life)
        val next = storageKey(preferences.key, wrap)
        assertEquals(replaced.key.encoded.toList(), next.key.encoded.toList())
        assertEquals(KeyLife.Kept, next.life)
    }

    @Test
    fun aDeletedSavedKeyIsNotTakenForTheFirstStart() {
        val preferences = Saved()
        val wrap = XorWrap()
        storageKey(preferences.key, wrap)
        preferences.value = null

        assertEquals(KeyLife.Kept, storageKey(preferences.key, wrap).life)
    }

    @Test
    fun aKeystoreThatFailsOnceLeavesTheSavedKeyAlone() {
        val preferences = Saved()
        val wrap = XorWrap()
        val first = storageKey(preferences.key, wrap)
        wrap.unwrapped = Unwrapped.Failed

        val meanwhile = storageKey(preferences.key, wrap)
        wrap.unwrapped = null

        assertNotEquals(first.key.encoded.toList(), meanwhile.key.encoded.toList())
        assertEquals(first.key.encoded.toList(), storageKey(preferences.key, wrap).key.encoded.toList())
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

    @Test
    fun aFailedFirstSaveDeletesTheNewWrappingKeySoTheNextStartIsFirst() {
        val preferences = Saved().apply { saves = false }
        val wrap = XorWrap()

        val failed = storageKey(preferences.key, wrap)
        assertEquals(KeyLife.ProcessOnly, failed.life)
        assertNull(preferences.value)

        preferences.saves = true
        val next = storageKey(preferences.key, wrap)

        assertEquals(KeyLife.First, next.life)
        assertEquals(next.key.encoded.toList(), storageKey(preferences.key, wrap).key.encoded.toList())
    }

    @Test
    fun aFailedSaveAfterTheFirstStartKeepsTheExistingWrappingKey() {
        val preferences = Saved()
        val wrap = XorWrap()
        storageKey(preferences.key, wrap)
        preferences.value = null
        preferences.saves = false

        val failed = storageKey(preferences.key, wrap)
        assertEquals(KeyLife.ProcessOnly, failed.life)

        preferences.saves = true
        assertEquals(KeyLife.Kept, storageKey(preferences.key, wrap).life)
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

    /** Stands in for the Keystore: it hides the key's bytes, keeps a wrapping key, and fails as asked. */
    private class XorWrap(
        var unwrapped: Unwrapped? = null,
        var wraps: Boolean = true,
        var invalidated: Boolean = false,
    ) : KeyWrap {
        private var hasKey = false
        private var generation = 0

        override fun wrap(key: ByteArray): Wrapped {
            if (!wraps) throw GeneralSecurityException("no keystore")
            if (hasKey && invalidated) throw GeneralSecurityException("key invalidated")
            val madeKey = !hasKey
            if (madeKey) {
                hasKey = true
                invalidated = false
                generation++
            }
            val mask = MASK xor generation
            return Wrapped("$generation:" + key.joinToString(",") { (it.toInt() xor mask).toString() }, madeKey)
        }

        override fun unwrap(wrapped: String): Unwrapped {
            val forced = unwrapped
            if (forced != null) return forced
            if (!hasKey || invalidated) return Unwrapped.Lost
            val parts = wrapped.split(":")
            if (parts.size != 2 || parts[0].toIntOrNull() != generation) return Unwrapped.Lost
            val mask = MASK xor generation
            return Unwrapped.Key(parts[1].split(",").map { (it.toInt() xor mask).toByte() }.toByteArray())
        }

        override fun forget() {
            hasKey = false
            invalidated = false
        }

        private companion object {
            const val MASK = 0x5a
        }
    }
}
