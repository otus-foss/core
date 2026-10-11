package com.otus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Flat settings rows: a title and description beside a control, separated by hairlines. */
public object SettingTokens {
    public val titleSize: TextUnit = 17.sp
    public val titleLineHeight: TextUnit = 22.sp
    public val descriptionSize: TextUnit = 14.sp
    public val descriptionLineHeight: TextUnit = 19.sp
    public val sectionTitleSize: TextUnit = 22.sp
    public val sectionTitleLineHeight: TextUnit = 28.sp

    /** Space above a section title, and between it and the section's first row. */
    public val sectionGap: Dp = 24.dp
    public val sectionTitleGap: Dp = 8.dp
    public val rowPadding: Dp = 16.dp
    public val rowGap: Dp = 16.dp
    public val textGap: Dp = 4.dp
    public val dividerWidth: Dp = 1.dp

    public val valueRadius: Dp = 8.dp
    public val valuePaddingHorizontal: Dp = 12.dp
    public val valuePaddingVertical: Dp = 8.dp
    public val valueMaxWidth: Dp = 180.dp

    public val switchWidth: Dp = 52.dp
    public val switchHeight: Dp = 32.dp
    public val switchThumb: Dp = 24.dp
    public val switchInset: Dp = 4.dp
    public const val SWITCH_MILLIS: Int = 150
}
