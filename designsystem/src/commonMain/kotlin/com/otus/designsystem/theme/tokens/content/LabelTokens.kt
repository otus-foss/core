package com.otus.designsystem.theme.tokens.content

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Read-only label pills, such as the folders a message row names. */
internal object LabelTokens {
    public val height: Dp = 20.dp
    public val horizontalPadding: Dp = 8.dp
    public val iconSize: Dp = 14.dp
    public val iconGap: Dp = 4.dp
    public val textSize: TextUnit = 12.sp

    // A count pill, such as the number of mails in a conversation.
    public val countHeight: Dp = 18.dp
    public val countPadding: Dp = 6.dp
    public val countTextSize: TextUnit = 11.sp

    /** A count in a circle, as on the Unread pill. */
    public val badgeSize: Dp = 22.dp
    public val badgePadding: Dp = 5.dp
    public val badgeTextSize: TextUnit = 12.sp
}
