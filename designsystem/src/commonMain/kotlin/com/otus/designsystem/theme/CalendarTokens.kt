package com.otus.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

public object CalendarTokens {
    public const val MONTH_COLUMN_WEIGHT: Float = 2f
    public const val CONTROLS_HEIGHT_FRACTION: Float = 0.35f
    public const val MIN_YEAR: Int = 1970
    public const val PICKER_HEIGHT_FRACTION: Float = 0.70f
    public val dragScrollEdge: Dp = 40.dp
    public val dragScrollSpeed: Dp = 360.dp
    public const val NANOS_PER_SECOND: Float = 1_000_000_000f
    public const val MONTH_LABEL_LENGTH: Int = 3
    public val monthLabelSize: TextUnit = 12.sp
    public val monthLabelLineHeight: TextUnit = 12.sp
    public val monthBackground: Color = OtusColors.bgDark
    public val alternateMonthBackground: Color = OtusColors.bgRegular
    public val monthCornerRadius: Dp = 8.dp
    public val markerEdgeInset: Dp = 2.dp
    public val endpointRadius: Dp = 4.dp
    public val rangeInset: Dp = 4.dp
    public const val DAYS_PER_WEEK: Int = 7
    public const val HALF_TURN: Float = 180f
}
