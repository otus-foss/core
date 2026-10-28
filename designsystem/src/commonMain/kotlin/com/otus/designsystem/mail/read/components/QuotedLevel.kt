package com.otus.designsystem.mail.read.components

import com.otus.designsystem.shared.people.components.NamedAvatar

/**
 * One level of quoted text in a reply: who wrote it, [person], null when the quote does not say,
 * its [date], and its own [text] without the levels quoted inside it. [onPerson] runs when the
 * person's pill is tapped; null leaves the pill inert. [fullDate], such as "17 Oct 2026, 17:50",
 * shows in place of the date after a tap on the button beside it.
 */
public class QuotedLevel(
    public val person: NamedAvatar?,
    public val date: String,
    public val text: String,
    public val onPerson: (() -> Unit)? = null,
    public val fullDate: String = date,
)
