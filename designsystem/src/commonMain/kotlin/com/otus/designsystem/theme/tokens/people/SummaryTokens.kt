package com.otus.designsystem.theme.tokens.people

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.tokens.layout.PanelTokens

/** The people line of a summary card: small avatars, each before its name. */
internal object SummaryTokens {
    val avatarSize: Dp = PanelTokens.iconSize
    val avatarGap: Dp = 6.dp
    val personGap: Dp = 12.dp

    /** People shown by name; the rest are counted as "+N". */
    const val PEOPLE_LIMIT: Int = 3
}
