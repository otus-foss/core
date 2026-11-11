package com.otusfoss.core.designsystem.shared.actionbar.components

import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import org.jetbrains.compose.resources.DrawableResource

/**
 * One [action] in an [ActionBar]. A secondary action shows only its symbol, which it must have,
 * and names itself with its label to accessibility services and in a long-press tooltip; the
 * primary shows its label when the bar has no context. A disabled action keeps its place. An action
 * with a [menu] opens it in a [ContextMenu][com.otusfoss.core.designsystem.shared.overlays.components.ContextMenu]
 * above the button instead of running its own click.
 */
public class BarAction(
    public val action: UiAction,
    public val menu: List<UiAction> = emptyList(),
) {
    internal val symbol: DrawableResource = checkNotNull(action.symbol) { "A bar action needs a symbol" }
}

/**
 * What the primary action applies to, shown in the shell beside it: a [token] and a [subtitle]
 * under the primary's label, such as "5 people". Accessibility services read [description] after
 * the label; pass it when the subtitle leaves out what the token shows, such as a count. A [lead]
 * takes the label's place as a small line above the subtitle, which then reads larger, as in
 * "Send to" above "Grace". Non-empty [choices] make the context its own button, which opens
 * them in a [ContextMenu][com.otusfoss.core.designsystem.shared.overlays.components.ContextMenu] above the
 * shell, such as other address books to save to, while the action keeps running the primary.
 */
public class BarContext(
    public val subtitle: String,
    public val token: BarToken,
    public val description: String = subtitle,
    public val lead: String? = null,
    public val choices: List<UiAction> = emptyList(),
)
