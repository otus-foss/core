package com.otusfoss.core.designsystem.theme.tokens.rows

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.theme.tokens.layout.CornerTokens
import com.otusfoss.core.designsystem.theme.tokens.layout.PanelTokens

/** Sizes of an entry card, the card behind each mail or person in a list; see EntryCard. */
public object EntryTokens {
    /** Rounding of an entry card where no other entry touches it, matching the panels. */
    public val radius: Dp = PanelTokens.panelRadius

    /** Rounding where another entry of the same run sits right above or below. */
    public val innerRadius: Dp = CornerTokens.tiny

    /** The space between entry cards, small enough that a run reads as one group. */
    public val gap: Dp = 2.dp

    /** The outline that marks a card on a theme whose raised color is its page color, such as Black. */
    public val outline: Dp = 1.dp

    public val avatarSize: Dp = 40.dp
    public val contentGap: Dp = 12.dp
    public val paddingStart: Dp = 14.dp
    public val paddingEnd: Dp = 12.dp
    public val paddingVertical: Dp = 10.dp

    // The taller EntrySize.Comfortable.
    public val comfortableAvatarSize: Dp = 48.dp
    public val comfortablePaddingVertical: Dp = 16.dp

    /** Rounding of a selected flat row's tint, matching the panels. */
    public val selectionRadius: Dp = PanelTokens.panelRadius
}
