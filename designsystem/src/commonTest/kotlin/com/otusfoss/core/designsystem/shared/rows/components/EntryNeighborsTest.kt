package com.otusfoss.core.designsystem.shared.rows.components

import kotlin.test.Test
import kotlin.test.assertEquals

class EntryNeighborsTest {
    @Test
    fun aLoneEntryTouchesNoOne() {
        assertEquals(EntryNeighbors(above = false, below = false), entryNeighbors(0, 1))
    }

    @Test
    fun onlyTheEndsOfARunHaveAnOpenSide() {
        assertEquals(EntryNeighbors(above = false, below = true), entryNeighbors(0, 3))
        assertEquals(EntryNeighbors(above = true, below = true), entryNeighbors(1, 3))
        assertEquals(EntryNeighbors(above = true, below = false), entryNeighbors(2, 3))
    }
}
