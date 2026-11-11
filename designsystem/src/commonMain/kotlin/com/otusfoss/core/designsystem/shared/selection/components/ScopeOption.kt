package com.otusfoss.core.designsystem.shared.selection.components

import com.otusfoss.core.designsystem.shared.people.components.ChipAvatar

/**
 * How one choice in a [ListSheet][com.otusfoss.core.designsystem.shared.overlays.components.ListSheet]
 * reads: [avatars], such as one per account, then [title] over an optional `text2` [subtitle], then
 * an optional [count]. A [pinned] choice shows a pin.
 */
public data class ScopeOption(
    val title: String,
    val avatars: List<ChipAvatar>,
    val subtitle: String? = null,
    val count: String? = null,
    val pinned: Boolean = false,
)
