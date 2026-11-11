package com.otusfoss.core.avatar

/**
 * The size and creation time of each record file of a [FileAvatarStore], by file name. The store
 * reads them from disk once, so a write finds the records to prune without opening every other one.
 */
internal class AvatarDiskIndex(
    private val maxBytes: Long,
    private val maxEntries: Int,
) {
    private val records = mutableMapOf<String, IndexedRecord>()
    private var bytes = 0L

    fun put(
        name: String,
        size: Long,
        created: Long,
    ) {
        remove(name)
        records[name] = IndexedRecord(size, created)
        bytes += size
    }

    fun remove(name: String) {
        val removed = records.remove(name) ?: return
        bytes -= removed.size
    }

    fun clear() {
        records.clear()
        bytes = 0
    }

    /** Removes the oldest records until the rest fit the limits, and returns their names. */
    fun prune(): List<String> {
        if (fits()) return emptyList()
        val oldest = records.entries.sortedBy { it.value.created }.map { it.key }
        val pruned = mutableListOf<String>()
        for (name in oldest) {
            if (fits()) break
            remove(name)
            pruned += name
        }
        return pruned
    }

    private fun fits() = bytes <= maxBytes && records.size <= maxEntries

    private class IndexedRecord(
        val size: Long,
        val created: Long,
    )
}
