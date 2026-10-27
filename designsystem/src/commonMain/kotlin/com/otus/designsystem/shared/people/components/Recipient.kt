package com.otus.designsystem.shared.people.components

/** A person to display: the [name] its chip shows and the [description] read aloud. */
public class Recipient(
    public val name: String,
    public val avatar: ChipAvatar,
    public val description: String,
)
