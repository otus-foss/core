package com.otus.designsystem.theme.tokens.overlays

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otus.designsystem.theme.tokens.layout.CornerTokens

internal object ChoiceSheetTokens {
    val handleWidth = 36.dp
    val handleHeight = 4.dp
    val handleTop = 8.dp
    val headerHeight = 64.dp
    val titleSize = 18.sp
    val optionMinHeight = 56.dp
    val optionPadding = 16.dp
    val optionGap = 12.dp

    /** An `IconChoice`'s padding around its icon and label, and the gap between them. */
    val iconChoicePadding = 12.dp
    val iconChoiceGap = 8.dp
    val iconChoiceRadius = CornerTokens.row

    /** Space between the default option's group and the rest. */
    val groupGap = 20.dp

    /** A downward fling faster than this closes the sheet, wherever it is released. */
    val dismissVelocity = 600.dp

    /** Dragged further down than this share of its height, the sheet closes on release. */
    const val DISMISS_FRACTION = 0.25f
}
