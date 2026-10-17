package com.otus.designsystem.components

/** A person on a [RecipientLine]: the [name] its chip shows and the [description] read aloud. */
public class Recipient(
    public val name: String,
    public val avatar: ChipAvatar,
    public val description: String,
)

/**
 * The address being typed on a [RecipientLine]. [text] is the caller's; [onCommit] runs on Next
 * and when the field loses focus, to turn the text into chips, and Backspace in an empty field
 * runs [onRemoveLast].
 */
public class AddressInput(
    public val text: String,
    public val onTextChange: (String) -> Unit,
    public val onCommit: () -> Unit,
    public val onRemoveLast: () -> Unit,
)
