package com.otusfoss.core.designsystem.shared.reorder.components

/** Where a row dragged in a [ReorderableTree] lands, by the index of a row in its items. */
public sealed interface TreeDropTarget {
    public val index: Int

    /**
     * In the gap above the row at [index], as a sibling of that row placed right before it. An
     * [index] equal to the number of items means after the last row, as that row's sibling.
     */
    public data class Before(
        override val index: Int,
    ) : TreeDropTarget

    /** Onto the row at [index], as its first child. */
    public data class Into(
        override val index: Int,
    ) : TreeDropTarget
}
