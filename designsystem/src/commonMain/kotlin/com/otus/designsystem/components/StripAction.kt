package com.otus.designsystem.components

import org.jetbrains.compose.resources.DrawableResource

/** The button at the end of an [InfoStrip], such as Unsubscribe or Archive all. */
public class StripAction(
    public val label: String,
    public val icon: DrawableResource,
    public val onClick: () -> Unit,
)
