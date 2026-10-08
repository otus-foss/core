package com.otus.designsystem.components

/** One button in an [ActionDialog]. Choosing it also closes the dialog. */
public class DialogAction(
    public val label: String,
    public val onClick: () -> Unit,
    public val primary: Boolean = false,
)
