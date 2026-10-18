package com.otus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** A dismissible card above a list that suggests one setting, such as a folder color. */
public object NudgeTokens {
    public val radius: Dp = CornerTokens.large
    public val paddingVertical: Dp = 12.dp
    public val paddingHorizontal: Dp = 14.dp
    public val iconSize: Dp = 20.dp
    public val iconGap: Dp = 10.dp
    public val swatchSize: Dp = 28.dp
    public val swatchGap: Dp = 8.dp
    public val rowGap: Dp = 8.dp
    public val textSize: TextUnit = 14.sp
    public val lineHeight: TextUnit = 20.sp

    /** Side inset of a nudge inside a list, and its gap to the first row. */
    public val listInset: Dp = 6.dp
    public val listGap: Dp = 8.dp
}
