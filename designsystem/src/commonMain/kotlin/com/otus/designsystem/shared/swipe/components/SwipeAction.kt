package com.otus.designsystem.shared.swipe.components

import org.jetbrains.compose.resources.DrawableResource

/**
 * An action revealed by swiping a row in [SwipeActions]. A [dismisses] action is for actions that
 * remove the row from its list: [onSwipe] runs after the flash, and the row stays at its revealed
 * width until the list removes it. Others run at once and spring the row back.
 */
public class SwipeAction(
    public val label: String,
    public val icon: DrawableResource,
    public val dismisses: Boolean = false,
    public val onSwipe: () -> Unit,
)
