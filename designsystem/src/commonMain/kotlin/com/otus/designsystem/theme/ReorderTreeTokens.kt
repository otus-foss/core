package com.otus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The drop line of a reorderable tree, lined up with the folder icons of its folder rows. */
internal object ReorderTreeTokens {
    /** Each nesting level moves the line as far as it moves a folder row's icon. */
    val indentStep: Dp = FolderRowTokens.nestIndent

    /** Where a top-level line starts: the start of the folder icon centered in its touch target. */
    val lineStart: Dp = (PanelTokens.touchTarget - PanelTokens.iconSize) / 2
    val lineEnd: Dp = FolderRowTokens.endPadding
    val lineThickness: Dp = 2.dp

    /** The dragged row lets the row it is over, and the line, show through. */
    const val DRAGGED_ALPHA: Float = 0.75f

    /** The ring at the line's start, drawn with the line's thickness. */
    val ringRadius: Dp = 3.dp
}
