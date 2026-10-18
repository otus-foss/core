package com.otus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Sizes of the filter panel that opens out of the inbox's control panel, and of its chips. */
public object FilterTokens {
    public val chipHeight: Dp = 36.dp
    public val chipIconSize: Dp = 18.dp

    /** The avatar before an address in a chip. */
    public val chipAvatarSize: Dp = 22.dp
    public val chipGap: Dp = 8.dp
    public val chipStartWithIcon: Dp = 10.dp
    public val chipStart: Dp = 14.dp
    public val chipEnd: Dp = 14.dp
    public val chipIconGap: Dp = 6.dp
    public val chipTextSize: TextUnit = 14.sp
    public val sectionGap: Dp = 16.dp

    /** Between a section's label and its chips. */
    public val labelGap: Dp = 8.dp
    public val labelInset: Dp = 4.dp
    public val labelSize: TextUnit = 13.sp
    public val headerHeight: Dp = 48.dp
    public val titleInset: Dp = 16.dp
    public val closeIconSize: Dp = 24.dp
    public val titleSize: TextUnit = 17.sp
    public val bodyInset: Dp = 12.dp
    public val footerPadding: Dp = 12.dp
    public val footerButtonHeight: Dp = 56.dp
    public val footerTextSize: TextUnit = 16.sp
    public val segmentHeight: Dp = 40.dp
    public val searchHeight: Dp = 48.dp
    public val searchRadius: Dp = CornerTokens.button
    public val folderRowHeight: Dp = 48.dp
    public val folderIndent: Dp = 16.dp

    /** How far the panel keeps from the window's top and bottom edges. */
    public val screenInset: Dp = 16.dp

    /** How far the panel's contents slide while they fade in. */
    public val contentSlide: Dp = 8.dp

    /** An upward swipe on the panel longer than this closes it. */
    public val closeSwipe: Dp = 72.dp
    public val badgeSize: Dp = 16.dp
    public val badgeTextSize: TextUnit = 10.sp
    public val progressSize: Dp = 16.dp
    public val progressStroke: Dp = 2.dp
}
