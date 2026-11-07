package com.otus.designsystem.theme.tokens.core

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The spacing scale. Feature tokens name a step, or one of the roles below, instead of repeating
 * a value, so the gutter or an indent changes here.
 */
public object Spacing {
    public val xs: Dp = 4.dp
    public val s: Dp = 6.dp
    public val l: Dp = 12.dp
    public val xl: Dp = 16.dp

    /** The inset of pages, sheets, menus, and rows from a screen or card edge. */
    public val gutter: Dp = xl

    /** How far each level of a tree, such as a folder tree, moves in. */
    public val indent: Dp = xl

    /** Between an icon and its label. */
    public val iconGap: Dp = xs

    /** Between items in a row, such as chips, or an avatar and a name. */
    public val inlineGap: Dp = s
}
