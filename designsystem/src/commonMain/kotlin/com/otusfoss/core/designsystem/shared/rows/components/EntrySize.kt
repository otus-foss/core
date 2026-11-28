package com.otusfoss.core.designsystem.shared.rows.components

import androidx.compose.ui.unit.Dp
import com.otusfoss.core.designsystem.theme.tokens.rows.EntryTokens

/** How tall an [EntryCard] is: its [avatarSize], which the caller's avatar takes too, and its padding. */
public enum class EntrySize(
    public val avatarSize: Dp,
    internal val paddingVertical: Dp,
) {
    Compact(EntryTokens.avatarSize, EntryTokens.paddingVertical),
    Comfortable(EntryTokens.comfortableAvatarSize, EntryTokens.comfortablePaddingVertical),
}
