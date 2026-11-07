package com.otus.designsystem.theme.tokens.core

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Sizes every component shares: touch targets, icons, outlines, and chips. */
public object Sizes {
    public val touchTarget: Dp = 48.dp
    public val iconSize: Dp = 20.dp
    public val borderWidth: Dp = 1.dp
    public val chipHeight: Dp = 36.dp

    /**
     * One row of a folder list: in the sidebar, the move sheet, and settings. Rows follow each
     * other without gaps, so they are shorter than [touchTarget] to fit more folders on screen.
     */
    public val compactRowHeight: Dp = 36.dp
    public val edgeFade: Dp = 24.dp
}
