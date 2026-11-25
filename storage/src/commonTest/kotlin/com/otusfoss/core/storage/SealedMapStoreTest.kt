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
    fun aStoreSavedBeforeSealingKeepsItsValuesAndIsSealedOnTheFirstRead() {
        plain.write(mapOf("draft" to "hello"))

        assertEquals(mapOf("draft" to "hello"), store.read())

        assertEquals(setOf("sealed"), plain.read().keys)
        assertEquals(mapOf("draft" to "hello"), store.read())
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

    private class MemoryMap : StringMapStore {
        private var values = emptyMap<String, String>()

        override fun read(): Map<String, String> = values

        override fun write(values: Map<String, String>) {
            this.values = values
        }
    }
}
