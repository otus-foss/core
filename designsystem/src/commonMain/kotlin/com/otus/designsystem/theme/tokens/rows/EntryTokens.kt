package com.otus.designsystem.theme.tokens.rows

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.tokens.layout.PanelTokens

/** Sizes of an entry card, the card behind each mail or person in a list; see EntryCard. */
public object EntryTokens {
    /** Rounding of an entry card, matching the panels. */
    public val radius: Dp = PanelTokens.panelRadius

    /** The space between entry cards, so each entry reads as its own item. */
    public val gap: Dp = 4.dp

    /** The outline that marks a card on a theme whose raised color is its page color, such as Black. */
    public val outline: Dp = 1.dp

    public val avatarSize: Dp = 40.dp
    public val contentGap: Dp = 12.dp
    public val paddingStart: Dp = 14.dp
    public val paddingEnd: Dp = 12.dp
    public val paddingVertical: Dp = 10.dp

    /** Rounding of a selected flat row's tint, matching the panels. */
    public val selectionRadius: Dp = PanelTokens.panelRadius
}
