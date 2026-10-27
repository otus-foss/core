package com.otus.designsystem.shared.rows.components

/**
 * Selection state of a content row while its list is selecting. [joinedAbove] and [joinedBelow]
 * say a selected neighbor touches the row there, so their tints merge into one block with square
 * corners where they meet.
 */
public data class RowSelection(
    public val selected: Boolean,
    public val joinedAbove: Boolean = false,
    public val joinedBelow: Boolean = false,
)

/**
 * The [RowSelection] of the entry at [index] in a run of [keys], joined to each neighbor that is
 * selected too, or null while nothing is [selected]. Pass each run apart, such as the entries
 * under one heading, so selections never join across the heading.
 */
public fun <K> rowSelection(
    keys: List<K>,
    index: Int,
    selected: Set<K>,
): RowSelection? {
    if (selected.isEmpty()) return null
    val isSelected = keys[index] in selected

    fun joined(offset: Int) = isSelected && keys.getOrNull(index + offset)?.let { it in selected } == true
    return RowSelection(isSelected, joinedAbove = joined(-1), joinedBelow = joined(1))
}
