package com.otus.storage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StoredChoiceTest {
    @Test
    fun aChoiceReadsBackWhatWasWritten() {
        val values = InMemoryKeyValueStore()
        StoredChoice(values, "theme").write("ink")
        assertEquals("ink", StoredChoice(values, "theme").read())
        assertNull(StoredChoice(values, "icon").read())
    }

    @Test
    fun anInMemoryChoiceStartsWithItsValue() {
        assertEquals("fast", inMemoryChoice("fast").read())
        assertNull(inMemoryChoice().read())
    }
}
