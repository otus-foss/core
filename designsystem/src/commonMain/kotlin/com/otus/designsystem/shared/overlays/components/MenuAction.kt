package com.otus.designsystem.shared.overlays.components

import org.jetbrains.compose.resources.DrawableResource

/**
 * One choice of a [ContextMenu] or
 * [QuickActionBar][com.otus.designsystem.shared.actionbar.components.QuickActionBar]. Choosing it
 * closes the menu first. A [ContextMenu] row leads with the [icon] when it has one; a
 * [QuickActionBar][com.otus.designsystem.shared.actionbar.components.QuickActionBar] shows only the
 * [icon] and reads the [label] to screen readers.
 */
public class MenuAction(
    public val label: String,
    public val icon: DrawableResource? = null,
    public val onClick: () -> Unit,
)
