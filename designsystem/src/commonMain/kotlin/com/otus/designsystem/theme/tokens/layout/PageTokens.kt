package com.otus.designsystem.theme.tokens.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Layout for pages built from a context panel header and a scrolling body. */
public object PageTokens {
    public val pageInset: Dp = 8.dp
    public val contentInset: Dp = 16.dp

    /** Free-standing text lines up with the text inside full-width rows. */
    public val textInset: Dp = 12.dp
    public val rowGap: Dp = 8.dp
    public val sectionGap: Dp = 24.dp
    public val captionSize: TextUnit = 14.sp
    public val dialogMaxWidth: Dp = 420.dp

    /** Each nesting level of a tree row moves its icon this far toward the trailing edge. */
    public val nestIndent: Dp = 16.dp
}
