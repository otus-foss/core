package com.otusfoss.core.designsystem.shared.rows.components

/**
 * Whether another entry of the same run sits right [above] and [below] an [EntryCard]. Where one
 * does, the card rounds less, so the run reads as one group with soft outer corners.
 */
public data class EntryNeighbors(
    public val above: Boolean = false,
    public val below: Boolean = false,
)

/**
 * The [EntryNeighbors] of the entry at [index] in a run of [count] entries. Pass each run apart,
 * such as the entries under one heading, so a heading ends the group.
 */
public fun entryNeighbors(
    index: Int,
    count: Int,
): EntryNeighbors = EntryNeighbors(above = index > 0, below = index < count - 1)
