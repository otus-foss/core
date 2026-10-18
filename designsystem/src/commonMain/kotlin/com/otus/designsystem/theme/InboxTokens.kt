package com.otus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

public object InboxTokens {
    public val pageInset: Dp = 8.dp
    public val contentInset: Dp = 16.dp
    public val itemGap: Dp = 12.dp
    public val avatarSize: Dp = 48.dp
    public val emptyIconSize: Dp = 64.dp
    public val rowPadding: Dp = 8.dp
    public val detailGap: Dp = 4.dp
    public val subjectSize: TextUnit = 20.sp
    public val senderSize: TextUnit = 14.sp
    public val metadataSize: TextUnit = 12.sp
    public val subjectLineHeight: TextUnit = 24.sp
    public val avatarRadius: Dp = 24.dp

    /** Rounding of a selected row's tint, matching the panels. */
    public val selectionRadius: Dp = PanelTokens.panelRadius

    // The compact two-line row; see CompactContentRow.
    public val compactAvatarSize: Dp = 40.dp
    public val compactPaddingVertical: Dp = 10.dp
    public val compactPaddingStart: Dp = 14.dp
    public val compactPaddingEnd: Dp = 12.dp
    public val lineGap: Dp = 3.dp
    public val markSize: Dp = 16.dp
    public val markGap: Dp = 4.dp
    public val compactTitleSize: TextUnit = 16.sp
    public val compactTitleLineHeight: TextUnit = 22.sp
    public val compactLineSize: TextUnit = 14.sp
    public val compactLineHeight: TextUnit = 20.sp
    public val compactDateSize: TextUnit = 13.sp
    public val moreFoldersSize: TextUnit = 11.sp

    /** A small avatar on another avatar's bottom-end corner, such as the account a mail came to. */
    public val badgeSize: Dp = 20.dp

    /** Negative, so the badge overhangs the avatar's edge on both axes. */
    public val badgeOffset: Dp = (-4).dp

    /** The background-colored ring that separates the badge from the avatar under it. */
    public val badgeRing: Dp = 2.dp
    public val badgeInitialsSize: TextUnit = 8.sp
}
