package com.otus.designsystem.components

import org.jetbrains.compose.resources.DrawableResource

/**
 * One action in an [ActionBar]. A secondary action shows only its [symbol] and names itself with
 * [label] to accessibility services and in a long-press tooltip; the primary shows its label when
 * the bar has no context. A disabled action keeps its place.
 */
public class BarAction(
    public val label: String,
    public val symbol: DrawableResource,
    public val enabled: Boolean = true,
    public val mirrorInRtl: Boolean = false,
    public val onClick: () -> Unit,
)

/**
 * What the primary action applies to, shown in the shell beside it: a [token] and a [subtitle]
 * under the primary's label, such as "5 people". Accessibility services read [description] after
 * the label; pass it when the subtitle leaves out what the token shows, such as a count. A [lead]
 * takes the label's place as a small line above the subtitle, which then reads larger, as in
 * "Send to" above "Grace".
 */
public class BarContext(
    public val subtitle: String,
    public val token: BarToken,
    public val description: String = subtitle,
    public val lead: String? = null,
)
