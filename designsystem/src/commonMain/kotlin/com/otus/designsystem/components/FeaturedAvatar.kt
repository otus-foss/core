package com.otus.designsystem.components

/** One face of [FeaturedAvatars]: its [avatar] over its [label]; [onClick] makes it a button. */
public class FeaturedAvatar(
    public val label: String,
    public val avatar: ChipAvatar,
    public val onClick: (() -> Unit)? = null,
)
