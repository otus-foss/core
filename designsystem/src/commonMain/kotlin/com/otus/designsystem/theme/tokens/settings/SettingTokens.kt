package com.otus.designsystem.theme.tokens.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otus.designsystem.theme.tokens.layout.CornerTokens

/** Flat settings rows: a title and description beside a control, separated by hairlines. */
internal object SettingTokens {
    val titleSize: TextUnit = 17.sp
    val titleLineHeight: TextUnit = 22.sp
    val descriptionSize: TextUnit = 14.sp
    val descriptionLineHeight: TextUnit = 19.sp
    val sectionTitleSize: TextUnit = 22.sp
    val sectionTitleLineHeight: TextUnit = 28.sp

    /** Space above a section title, and between it and the section's first row. */
    val sectionGap: Dp = 24.dp
    val sectionTitleGap: Dp = 8.dp

    /** An avatar before a section title, and the space between them. */
    val sectionAvatar: Dp = 28.dp
    val sectionLeadingGap: Dp = 12.dp
    val rowPadding: Dp = 16.dp

    /**
     * One 22 sp line between these paddings fills
     * [PanelTokens.compactRowHeight][com.otus.designsystem.theme.tokens.layout.PanelTokens.compactRowHeight].
     */
    val compactRowPadding: Dp = 7.dp
    val rowGap: Dp = 16.dp
    val textGap: Dp = 4.dp
    val dividerWidth: Dp = 1.dp

    val valueRadius: Dp = CornerTokens.chip
    val valuePaddingHorizontal: Dp = 12.dp
    val valuePaddingVertical: Dp = 8.dp
    val valuePaddingSwatch: Dp = 8.dp
    val valueSwatchGap: Dp = 8.dp
    val valueMaxWidth: Dp = 180.dp

    val switchWidth: Dp = 52.dp
    val switchHeight: Dp = 32.dp
    val switchThumb: Dp = 24.dp
    val switchInset: Dp = 4.dp
}
