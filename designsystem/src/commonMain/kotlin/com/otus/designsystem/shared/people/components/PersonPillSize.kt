package com.otus.designsystem.shared.people.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.otus.designsystem.theme.tokens.core.Spacing
import com.otus.designsystem.theme.tokens.people.PersonPillTokens
import com.otus.designsystem.theme.tokens.rows.StripTokens

/** How big a person pill is: [Small] inside text, such as a quote's author, or [Regular], like a recipient chip. */
internal enum class PersonPillSize(
    val height: Dp,
    val avatar: Dp,
    val start: Dp,
    val gap: Dp,
    val textSize: TextUnit,
) {
    Small(
        PersonPillTokens.quoteHeight,
        PersonPillTokens.quoteAvatarSize,
        PersonPillTokens.quoteStart,
        StripTokens.glyphGap,
        PersonPillTokens.quoteTextSize,
    ),
    Regular(
        PersonPillTokens.recipientHeight,
        PersonPillTokens.recipientAvatarSize,
        Spacing.iconGap,
        Spacing.inlineGap,
        TextUnit.Unspecified,
    ),
}
