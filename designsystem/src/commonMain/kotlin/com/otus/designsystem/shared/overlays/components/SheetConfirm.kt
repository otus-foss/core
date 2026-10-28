package com.otus.designsystem.shared.overlays.components

import org.jetbrains.compose.resources.DrawableResource

/** The full-width button at the bottom of a [ClosableSheet], such as Copy, with an optional [icon]. */
public class SheetConfirm(
    public val label: String,
    public val onConfirm: () -> Unit,
    public val enabled: Boolean = true,
    public val icon: DrawableResource? = null,
)
