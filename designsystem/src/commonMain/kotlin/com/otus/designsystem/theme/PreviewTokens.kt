package com.otus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A [com.otus.designsystem.shared.settings.components.PreviewCard] and the page sketch above its preview. */
internal object PreviewTokens {
    val sketchPadding: Dp = 10.dp
    val sketchRadius: Dp = CornerTokens.chip
    val lineHeight: Dp = 5.dp
    val lineGap: Dp = 6.dp
    val dotSize: Dp = 8.dp

    /** Widths of the sketch's two text lines, as shares of the space beside the dot. */
    const val FIRST_LINE: Float = 0.75f
    const val SECOND_LINE: Float = 0.55f
}
