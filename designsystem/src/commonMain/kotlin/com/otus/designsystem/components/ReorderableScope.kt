package com.otus.designsystem.components

import androidx.compose.runtime.Composable

/** What a [ReorderableColumn] row can place. */
public interface ReorderableScope {
    /**
     * The handle that drags this row. [label] names the row for accessibility services, which also
     * get Move up and Move down actions.
     */
    @Composable
    public fun DragHandle(label: String)
}
