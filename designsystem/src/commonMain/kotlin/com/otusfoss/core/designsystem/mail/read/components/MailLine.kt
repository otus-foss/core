package com.otusfoss.core.designsystem.mail.read.components

/** One mail in a [MoreMail] list: its [subject], the start of its text, its [date], and whether it is [unread]. */
public class MailLine(
    public val subject: String,
    public val preview: String,
    public val date: String,
    public val unread: Boolean = false,
)
