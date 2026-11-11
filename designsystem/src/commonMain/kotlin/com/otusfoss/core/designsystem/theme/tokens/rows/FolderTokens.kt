package com.otusfoss.core.designsystem.theme.tokens.rows

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Folder glyphs: the mark beside a folder in a row, and glyphs stacked in a strip. */
public object FolderTokens {
    /** Folder glyphs stacked in a strip, and how far each overlaps the one before. */
    public val glyphSize: Dp = 16.dp
    public val glyphOverlap: Dp = 4.dp
    public const val STACK_LIMIT: Int = 3

    /** The folder mark beside a name in a row. */
    public val markSize: Dp = 16.dp
}
