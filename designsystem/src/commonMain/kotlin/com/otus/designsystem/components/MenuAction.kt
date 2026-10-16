package com.otus.designsystem.components

/** One row of a [ContextMenu]. Choosing it closes the menu first. */
public class MenuAction(
    public val label: String,
    public val onClick: () -> Unit,
)
