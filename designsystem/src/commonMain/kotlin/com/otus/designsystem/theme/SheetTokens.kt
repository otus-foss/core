package com.otus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

public object SheetTokens {
    public val inset: Dp = 16.dp
    public val gap: Dp = 12.dp
    public val radius: Dp = CornerTokens.surface
    public val rowRadius: Dp = CornerTokens.row
    public val maxWidth: Dp = 560.dp
    public val rowHeight: Dp = 64.dp

    /** A one-line selection row; rows sit [gap] apart. */
    public val selectionRowHeight: Dp = 44.dp

    /** Above and below a two-line selection row's text. */
    public val rowPadding: Dp = 8.dp
    public val titleSize: TextUnit = 22.sp
    public val detailSize: TextUnit = 14.sp
    public const val HEIGHT_FRACTION: Float = 0.88f
    public const val SCRIM_ALPHA: Float = 0.65f
}
