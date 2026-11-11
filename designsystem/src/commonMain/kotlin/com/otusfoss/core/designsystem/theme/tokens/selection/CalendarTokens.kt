package com.otusfoss.core.designsystem.theme.tokens.selection

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.layout.CornerTokens

internal object CalendarTokens {
    public const val MONTH_COLUMN_WEIGHT: Float = 2f
    public const val YEAR_COLUMN_WEIGHT: Float = 1.25f
    public const val CONTROLS_HEIGHT_FRACTION: Float = 0.35f
    public const val MIN_YEAR: Int = 1970
    public const val PICKER_HEIGHT_FRACTION: Float = 0.55f
    public val pickerMinHeight: Dp = 440.dp
    public val dragScrollEdge: Dp = 40.dp
    public val dragScrollSpeed: Dp = 360.dp
    public const val NANOS_PER_SECOND: Float = 1_000_000_000f
    public const val MONTH_LABEL_LENGTH: Int = 3
    public val monthLabelSize: TextUnit = 12.sp
    public val monthLabelLineHeight: TextUnit = 12.sp
    public val monthBackground: Color get() = OtusColors.bgDark
    public val alternateMonthBackground: Color get() = OtusColors.bgRegular
    public val monthCornerRadius: Dp = CornerTokens.chip
    public val markerEdgeInset: Dp = 2.dp
    public val endpointRadius: Dp = CornerTokens.tiny
    public val rangeInset: Dp = 4.dp
    public const val DAYS_PER_WEEK: Int = 7

    /** How far a range's end days step from the range's `selectedFill` toward `text1`. */
    public const val ENDPOINT_STEP: Float = 0.25f
    public const val HALF_TURN: Float = 180f

    /** A date range calendar shown inline on a filter page: its heading, weekdays, and about four weeks. */
    public val rangeCalendarHeight: Dp = 360.dp
}
