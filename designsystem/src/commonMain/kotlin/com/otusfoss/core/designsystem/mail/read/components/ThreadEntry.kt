package com.otusfoss.core.designsystem.mail.read.components

import com.otusfoss.core.designsystem.shared.people.components.ChipAvatar

/** One message of a thread: who sent it, their avatar, when, and the start of its text. */
public class ThreadEntry(
    public val name: String,
    public val avatar: ChipAvatar,
    public val date: String,
    public val preview: String = "",
)
