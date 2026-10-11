package com.otus.designsystem.components

import org.jetbrains.compose.resources.DrawableResource

/**
 * An action revealed by swiping a row in [SwipeActions]. A [dismisses] action slides the row out
 * before [onSwipe], for actions that remove it from its list; others spring the row back.
 */
public class SwipeAction(
    public val label: String,
    public val icon: DrawableResource,
    public val dismisses: Boolean = false,
    public val onSwipe: () -> Unit,
)
