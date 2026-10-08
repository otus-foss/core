package com.otus.designsystem.components

/** One person in an [AvatarStackChip]: generated initials and optional encoded image bytes. */
public class ChipAvatar(
    public val initials: String,
    public val colorIndex: Int,
    public val imageBytes: ByteArray? = null,
)
