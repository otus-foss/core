package com.otusfoss.core.designsystem.mail.read.components

/**
 * One mail in a [MoreMail] or [RecentMail] list: its [subject], the start of its text, its [date],
 * whether it is [unread], and whether it is the [current] one, such as the thread the user reads.
 */
public class MailLine(
    public val subject: String,
    public val preview: String,
    public val date: String,
    public val unread: Boolean = false,
    public val current: Boolean = false,
)
