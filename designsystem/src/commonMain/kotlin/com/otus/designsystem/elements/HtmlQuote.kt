package com.otus.designsystem.elements

/**
 * The quoted part of a reply that an [HtmlView] folds under the body: who wrote it, by [name] and
 * [initials], the [date] as the reader should see it, such as "10 Oct", and its [html]. A blank
 * [name] or [date] is left out of the header.
 */
public class HtmlQuote(
    public val name: String,
    public val initials: String,
    public val date: String,
    public val html: String,
)
