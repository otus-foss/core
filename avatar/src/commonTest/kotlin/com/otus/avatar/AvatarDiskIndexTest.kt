package com.otus.avatar

import kotlin.test.Test
import kotlin.test.assertEquals

class AvatarDiskIndexTest {
    @Test
    fun prunesTheOldestRecordsOnlyOnceTheEntryLimitIsExceeded() {
        val index = AvatarDiskIndex(maxBytes = 1_000, maxEntries = 2)
        index.put("b", size = 10, created = 2)
        index.put("a", size = 10, created = 1)
        assertEquals(emptyList(), index.prune())
        index.put("c", size = 10, created = 3)
        assertEquals(listOf("a"), index.prune())
        assertEquals(emptyList(), index.prune())
    }

    @Test
    fun prunesByTotalSizeAndCountsAReplacedRecordOnce() {
        val index = AvatarDiskIndex(maxBytes = 25, maxEntries = 10)
        index.put("a", size = 10, created = 1)
        index.put("b", size = 10, created = 2)
        index.put("a", size = 12, created = 5)
        assertEquals(emptyList(), index.prune())
        index.put("c", size = 10, created = 3)
        assertEquals(listOf("b"), index.prune())
    }

    @Test
    fun removedAndClearedRecordsFreeTheirSpace() {
        val index = AvatarDiskIndex(maxBytes = 20, maxEntries = 10)
        index.put("a", size = 10, created = 1)
        index.put("b", size = 10, created = 2)
        index.remove("a")
        index.remove("missing")
        index.put("c", size = 10, created = 3)
        assertEquals(emptyList(), index.prune())
        index.clear()
        index.put("d", size = 20, created = 4)
        assertEquals(emptyList(), index.prune())
    }
}
