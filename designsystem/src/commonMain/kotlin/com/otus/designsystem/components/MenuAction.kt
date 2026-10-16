package com.otus.designsystem.components

import org.jetbrains.compose.resources.DrawableResource

/** One row of a [ContextMenu], led by its [symbol] when it has one. Choosing it closes the menu first. */
public class MenuAction(
    public val label: String,
    public val symbol: DrawableResource? = null,
    public val mirrorInRtl: Boolean = false,
    public val onClick: () -> Unit,
)
