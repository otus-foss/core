package com.otus.designsystem.theme.tokens.rows

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

public object InboxTokens {
    public val pageInset: Dp = 8.dp
    public val itemGap: Dp = 12.dp
    public val avatarSize: Dp = 48.dp
    public val rowPadding: Dp = 8.dp
    public val detailGap: Dp = 4.dp
    public val senderSize: TextUnit = 14.sp
    public val metadataSize: TextUnit = 12.sp
    public val subjectLineHeight: TextUnit = 24.sp
    public val avatarRadius: Dp = 24.dp

    // The compact two-line row; see CompactContentRow.
    public val compactAvatarSize: Dp = EntryTokens.avatarSize
    public val lineGap: Dp = 3.dp
    public val markGap: Dp = 4.dp
    public val compactTitleSize: TextUnit = 16.sp
    public val compactTitleLineHeight: TextUnit = 22.sp
    public val compactLineSize: TextUnit = 14.sp
    public val compactLineHeight: TextUnit = 20.sp
    public val compactDateSize: TextUnit = 13.sp
    public val moreFoldersSize: TextUnit = 11.sp
}
