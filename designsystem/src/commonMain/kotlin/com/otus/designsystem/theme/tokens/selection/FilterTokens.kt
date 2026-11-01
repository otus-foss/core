package com.otus.designsystem.theme.tokens.selection

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otus.designsystem.theme.tokens.layout.CornerTokens

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
    public val segmentHeight: Dp = 40.dp
    public val searchHeight: Dp = 48.dp
    public val searchRadius: Dp = CornerTokens.button
    public val folderRowHeight: Dp = 48.dp
    public val folderIndent: Dp = 16.dp

    /** How many levels a folder tree indents; deeper folders line up with the last level. */
    public const val FOLDER_MAX_DEPTH: Int = 5

    /** A row that opens a filter page and shows its current value. */
    public val valueRowHeight: Dp = 52.dp
    public val rowTitleSize: TextUnit = 15.sp

    /** A person, address, or account to pick on a filter page. */
    public val pickRowHeight: Dp = 56.dp
    public val pickAvatarSize: Dp = 36.dp

    /** The From and To column of a line of picked people. */
    public val peopleLabelWidth: Dp = 40.dp

    /** The least room the placeholder after picked people keeps, such as "Add people…". */
    public val placeholderMinWidth: Dp = 80.dp

    /** A picked person's name in a chip ends in an ellipsis past this width. */
    public val personChipNameMaxWidth: Dp = 160.dp
    public val cardRadius: Dp = CornerTokens.panel

    /** A date range calendar shown inline on a filter page: its heading, weekdays, and about four weeks. */
    public val rangeCalendarHeight: Dp = 360.dp

    /** A small note under a filter page's choices, such as which folders are left out. */
    public val noteSize: TextUnit = 12.sp

    /** How far each folder in a pill's stack overlaps the one before it. */
    public val folderStackOverlap: Dp = 8.dp
    public const val FOLDER_STACK_LIMIT: Int = 3

    /** How far the panel keeps from the window's top and bottom edges. */
    public val screenInset: Dp = 16.dp

    public val badgeSize: Dp = 16.dp
    public val badgeTextSize: TextUnit = 10.sp
}
