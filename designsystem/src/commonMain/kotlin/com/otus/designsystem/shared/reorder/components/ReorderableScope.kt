package com.otus.designsystem.shared.reorder.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** What a [ReorderableColumn] or [ReorderableTree] row can place. */
public interface ReorderableScope {
    /**
     * The handle that drags this row. [label] names the row for accessibility services, which also
     * get Move up and Move down actions.
     */
    @Composable
    public fun DragHandle(label: String)

    /**
     * Lets a long press anywhere on this row drag it, for rows without a visible handle. A tap still
     * reaches the row's own click. Accessibility services get Move up and Move down actions on the row.
     */
    public fun Modifier.dragOnLongPress(): Modifier
}
