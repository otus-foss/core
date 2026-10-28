package com.otus.designsystem.theme.tokens.rows

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.tokens.layout.PanelTokens

/** A folder on the Folders settings page: its icon toggle, name, an optional second line, and a chevron. */
internal object FolderRowTokens {
    val minHeight: Dp = 56.dp
    val verticalPadding: Dp = 3.dp

    /** Each nesting level moves the icon and name this far toward the trailing edge. */
    val nestIndent: Dp = 20.dp
    val nameGap: Dp = PanelTokens.iconGap
    val lineGap: Dp = 6.dp

    /** The trailing icons' 48 dp slots already keep them clear of the screen edge. */
    val endPadding: Dp = 0.dp
}
