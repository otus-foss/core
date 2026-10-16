package com.otus.designsystem.components

/**
 * How one choice in a [ScopeSheet] reads: [avatars], such as one per account, then [title] over
 * an optional `text2` [subtitle], then an optional [count]. A [pinned] choice shows a pin.
 */
public data class ScopeOption(
    val title: String,
    val avatars: List<ChipAvatar>,
    val subtitle: String? = null,
    val count: String? = null,
    val pinned: Boolean = false,
)
