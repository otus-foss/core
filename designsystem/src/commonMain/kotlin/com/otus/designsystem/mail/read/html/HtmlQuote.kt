package com.otus.designsystem.mail.read.html

/**
 * The quoted part of a reply that an `HtmlView` folds under the body: who wrote it, by [name] and
 * [initials], the [date] as the reader should see it, such as "10 Oct", and its [html]. A blank
 * [name] or [date] is left out of the header. The [deeper] levels it quotes follow its text once
 * unfolded, each under its own header. [fullDate] shows in place of the date after a tap on the
 * button beside it.
 */
public data class HtmlQuote(
    public val name: String,
    public val initials: String,
    public val date: String,
    public val html: String,
    public val deeper: List<HtmlQuoteLevel> = emptyList(),
    public val fullDate: String = date,
) {
    public companion object {
        /** A quote read as plain text: [first]'s text as its own, then the [deeper] levels it quotes. */
        public fun ofText(
            first: HtmlQuoteLevel,
            deeper: List<HtmlQuoteLevel>,
        ): HtmlQuote = HtmlQuote(first.name, first.initials, first.date, textHtml(first.text), deeper, first.fullDate)
    }
}

/**
 * One level of a quote read as plain text: who wrote it, by [name] and [initials], its [date], and
 * its own [text], without the levels quoted inside it, and its [fullDate] with the time.
 */
public data class HtmlQuoteLevel(
    public val name: String,
    public val initials: String,
    public val date: String,
    public val text: String,
    public val fullDate: String = date,
)
