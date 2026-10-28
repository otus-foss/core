package com.otus.designsystem.theme.tokens.content

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The earlier messages of a thread above a reply's action bar, and the sheet that lists them. */
public object ThreadTokens {
    /** Up to this many messages show as rows; more fold into one summary row. */
    public const val ROW_LIMIT: Int = 3

    /** A message row above the bar: its height and avatar. */
    public val rowHeight: Dp = 40.dp
    public val avatarSize: Dp = 22.dp

    /** The folded summary row's height. */
    public val summaryHeight: Dp = 56.dp

    /** The name, the date, and the summary's second line. */
    public val nameSize: TextUnit = 14.sp
    public val detailSize: TextUnit = 12.sp

    /** A message row in the sheet: its avatar. */
    public val sheetAvatarSize: Dp = 32.dp
}
