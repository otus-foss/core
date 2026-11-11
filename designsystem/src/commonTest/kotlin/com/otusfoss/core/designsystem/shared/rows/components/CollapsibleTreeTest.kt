package com.otusfoss.core.designsystem.shared.rows.components

import kotlin.test.Test
import kotlin.test.assertEquals

class CollapsibleTreeTest {
    private val tree =
        listOf(
            TreeEntry("c", 0),
            TreeEntry("ca", 1),
            TreeEntry("cai", 2),
            TreeEntry("cb", 1),
            TreeEntry("p", 0),
        )

    @Test
    fun onlyEntriesWithNestedEntriesCanCollapse() {
        assertEquals(setOf("c", "ca"), tree.parentIds())
    }

    @Test
    fun collapsingAnEntryHidesEverythingNestedUnderItOnly() {
        assertEquals(setOf("ca", "cai", "cb"), tree.collapsedAway(setOf("c")))
        assertEquals(setOf("cai"), tree.collapsedAway(setOf("ca")))
        // A collapsed entry inside a collapsed one changes nothing more.
        assertEquals(setOf("ca", "cai", "cb"), tree.collapsedAway(setOf("c", "ca")))
        assertEquals(emptySet(), tree.collapsedAway(setOf("p")))
    }
}
