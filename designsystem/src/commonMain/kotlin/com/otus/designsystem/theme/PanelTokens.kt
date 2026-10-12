package com.otus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

public object PanelTokens {
    public const val MODE_TRANSITION_MILLIS: Int = 220
    public val touchTarget: Dp = 48.dp
    public val chipHeight: Dp = 36.dp
    public val iconSize: Dp = 20.dp
    public val chipAvatarOverlap: Dp = 6.dp
    public const val CHIP_AVATAR_LIMIT: Int = 4
    public val navigationIconSize: Dp = 32.dp
    public val chipPadding: Dp = 10.dp
    public val chipDividerHeight: Dp = 14.dp
    public val iconGap: Dp = 4.dp
    public val chipGap: Dp = 6.dp
    public val panelInset: Dp = 8.dp
    public val headerHeight: Dp = touchTarget + panelInset * 2
    public val panelRadius: Dp = 22.dp
    public val detachedGap: Dp = 0.dp
    public val cutoutGap: Dp = 2.dp
    public val borderWidth: Dp = 1.dp
    public val fadeWidth: Dp = 24.dp
    public val textSize: TextUnit = 16.sp
    public val lineHeight: TextUnit = 20.sp

    /** A count beside a label, such as unread mail in a chip. */
    public val countSize: TextUnit = 14.sp
}
