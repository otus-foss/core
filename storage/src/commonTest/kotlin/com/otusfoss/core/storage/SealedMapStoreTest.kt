package com.otusfoss.core.storage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SealedMapStoreTest {
    private val plain = MemoryMap()
    private val cipher = MarkingTextCipher()
    private val store = SealedMapStore(plain, cipher, "outbox")

    @Test
    fun aWrittenMapReadsBackAndShowsNoKeyOrValueUnsealed() {
        val values = mapOf("a:1" to "first\nline", "" to "", "ключ" to "12:34")

        store.write(values)

        assertEquals(values, store.read())
        assertEquals(setOf("sealed"), plain.read().keys)
        assertTrue(isSealedText(plain.read().getValue("sealed")))
    }

    @Test
    fun anEmptyStoreReadsEmptyAndStaysUntouched() {
        assertEquals(emptyMap(), store.read())
        assertEquals(emptyMap(), plain.read())
    }

    @Test
    fun theFirstKeysProcessTakesInAStoreSavedBeforeSealing() {
        plain.write(mapOf("draft" to "hello"))

        val taken = SealedMapStore(plain, MarkingTextCipher(KeyLife.First), "outbox")

        assertEquals(setOf("sealed"), plain.read().keys)
        assertEquals(mapOf("draft" to "hello"), taken.read())
        assertEquals(mapOf("draft" to "hello"), store.read())
    }

    @Test
    fun anUnsealedStoreAfterTheFirstStartIsNeverUsed() {
        plain.write(mapOf("planted" to "send this"))

        assertEquals(emptyMap(), store.read())
        store.write(mapOf("draft" to "mine"))

        assertEquals(mapOf("draft" to "mine"), store.read())
        assertEquals(setOf("sealed"), plain.read().keys)
    }

    @Test
    fun aKeyForThisProcessOnlyNeverReplacesTheSealedStore() {
        store.write(mapOf("outbox" to "waits"))
        val meanwhile = SealedMapStore(plain, BrokenCipher(KeyLife.ProcessOnly), "outbox")

        assertEquals(emptyMap(), meanwhile.read())
        meanwhile.write(mapOf("mark" to "read"))

        assertEquals(mapOf("mark" to "read"), meanwhile.read())
        assertEquals(mapOf("outbox" to "waits"), store.read())
    }

    @Test
    fun aKeyLostForGoodLetsTheStoreStartOver() {
        store.write(mapOf("outbox" to "waits"))
        val replaced = SealedMapStore(plain, BrokenCipher(KeyLife.Kept), "outbox")

        replaced.write(mapOf("mark" to "read"))

        assertEquals(emptyMap(), store.read())
    }

    @Test
    fun aValueSealedForAnotherStoreReadsAsEmpty() {
        SealedMapStore(plain, cipher, "contacts").write(mapOf("a" to "b"))

        assertEquals(emptyMap(), store.read())
    }

    @Test
    fun brokenEntriesDoNotDecode() {
        assertNull(decodeEntries("3:ab"))
        assertNull(decodeEntries("x:ab"))
        assertNull(decodeEntries("1:a"))
        assertNull(decodeEntries("-1:"))
        assertEquals(emptyMap(), decodeEntries(""))
    }

    @Test
    fun theMarkingCipherRefusesAnotherContextAndPlainText() {
        assertNull(cipher.open(cipher.seal("text", "one"), "two"))
        assertNull(cipher.open("text", "one"))
        assertFalse(isSealedText("text"))
    }

    /** A cipher whose key cannot open what [MarkingTextCipher] sealed. */
    private class BrokenCipher(
        override val keyLife: KeyLife,
    ) : TextCipher {
        override fun seal(
            text: String,
            context: String,
        ): String = "${SEALED_PREFIX}other:$text"

        override fun open(
            sealed: String,
            context: String,
        ): String? = null
    }

    private class MemoryMap : StringMapStore {
        private var values = emptyMap<String, String>()

        override fun read(): Map<String, String> = values

        override fun write(values: Map<String, String>) {
            this.values = values
        }
    }
}
