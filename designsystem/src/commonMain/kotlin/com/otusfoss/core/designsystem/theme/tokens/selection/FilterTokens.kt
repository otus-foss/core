package com.otusfoss.core.designsystem.theme.tokens.selection

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.tokens.layout.CornerTokens

/** Sizes of the filter panel that opens out of the inbox's control panel. */
public object FilterTokens {
    public val chipGap: Dp = 8.dp
    public val sectionGap: Dp = 16.dp

    /** Between a section's label and its chips. */
    public val labelGap: Dp = 8.dp
    public val labelInset: Dp = 4.dp
    public val labelSize: TextUnit = 13.sp
    public val bodyInset: Dp = 12.dp
    public val footerPadding: Dp = 12.dp
    public val folderRowHeight: Dp = 48.dp
    public val folderIndent: Dp = Spacing.indent

    /** How many levels a folder tree indents; deeper folders line up with the last level. */
    public const val FOLDER_MAX_DEPTH: Int = 5

    /** A row that opens a filter page and shows its current value. */
    public val valueRowHeight: Dp = 52.dp
    public val rowTitleSize: TextUnit = 15.sp

    /** The From and To column of a line of picked people. */
    public val peopleLabelWidth: Dp = 40.dp

    /** The least room the placeholder after picked people keeps, such as "Add people…". */
    public val placeholderMinWidth: Dp = 80.dp

    public val cardRadius: Dp = CornerTokens.panel

    /** How far each folder in a pill's stack overlaps the one before it. */
    public val folderStackOverlap: Dp = 8.dp
    public const val FOLDER_STACK_LIMIT: Int = 3

    /** How far the panel keeps from the window's top and bottom edges. */
    public val screenInset: Dp = Spacing.gutter
}
