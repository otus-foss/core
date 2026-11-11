package com.otusfoss.core.designsystem.shared.swipe.components

import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import org.jetbrains.compose.resources.DrawableResource

/**
 * An [action] revealed by swiping a row in [SwipeActions]; it must have a symbol. A [dismisses]
 * action is for actions that remove the row from its list: it runs after the flash, and the row
 * stays at its revealed width until the list removes it, the row's state changes, or a second
 * passes. Others run at once and spring the row back.
 */
public class SwipeAction(
    public val action: UiAction,
    public val dismisses: Boolean = false,
) {
    internal val symbol: DrawableResource = checkNotNull(action.symbol) { "A swipe action needs a symbol" }
}
