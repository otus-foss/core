package com.otus.designsystem.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.otus.designsystem.theme.ComposeTokens
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.ReadTokens

/** How big a person pill is: [Small] inside text, such as a quote's author, or [Regular], like a recipient chip. */
internal enum class PersonPillSize(
    val height: Dp,
    val avatar: Dp,
    val start: Dp,
    val gap: Dp,
    val textSize: TextUnit,
) {
    Small(
        ReadTokens.quotePillHeight,
        ReadTokens.quoteAvatarSize,
        ReadTokens.quotePillStart,
        ReadTokens.glyphGap,
        ReadTokens.quoteTextSize,
    ),
    Regular(
        ComposeTokens.recipientHeight,
        ComposeTokens.recipientAvatarSize,
        PanelTokens.iconGap,
        PanelTokens.chipGap,
        TextUnit.Unspecified,
    ),
}
