package com.otusfoss.core.designsystem.theme.tokens.people

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The sheet about one person: its row of action and count tiles, and the people they share threads with. */
internal object PersonSheetTokens {
    val tileHeight: Dp = 68.dp
    val tileGap: Dp = 8.dp
    val tileLabelGap: Dp = 4.dp
    val countSize: TextUnit = 20.sp
    val labelSize: TextUnit = 12.sp

    /** The faces before "Also in threads with". */
    val faceSize: Dp = 24.dp
}
