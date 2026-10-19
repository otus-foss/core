package com.otus.designsystem.components

/**
 * One level of quoted text in a reply: who wrote it, [person], null when the quote does not say,
 * its [date], and its own [text] without the levels quoted inside it. [onPerson] runs when the
 * person's pill is tapped; null leaves the pill inert.
 */
public class QuotedLevel(
    public val person: NamedAvatar?,
    public val date: String,
    public val text: String,
    public val onPerson: (() -> Unit)? = null,
)
