package com.otus.designsystem.shared.overlays.components

import org.jetbrains.compose.resources.DrawableResource

/**
 * What finishes a [SheetFrame], such as Done or Copy: its [label], the action, and whether it is
 * [enabled]. The [icon] shows on the full-width button of a sheet with a close button.
 */
public class SheetConfirm(
    public val label: String,
    public val onConfirm: () -> Unit,
    public val enabled: Boolean = true,
    public val icon: DrawableResource? = null,
)
