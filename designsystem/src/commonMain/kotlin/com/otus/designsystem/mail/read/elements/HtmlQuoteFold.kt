package com.otus.designsystem.mail.read.elements

import com.otus.designsystem.theme.colors.OtusPalette
import com.otus.designsystem.theme.tokens.content.ReadTokens
import com.otus.designsystem.theme.tokens.layout.PanelTokens

/**
 * The fold for [quote] after the body: a header pill with the author's initials and name and the
 * date, each left out when blank, the quote cut to a few lines that fade out, and a toggle that
 * unfolds it. The toggle is a `<details>` element, so it works without scripts and reads as an
 * expandable button. It comes first in the markup and shows below the quote; the quote stays
 * readable to a screen reader while folded, since it is only clipped.
 */
internal fun quoteFold(
    quote: HtmlQuote,
    palette: OtusPalette,
    key: RenderKey,
): String {
    val fold = key.className("quote")
    val toggle = key.className("toggle")
    val body = key.className("body")
    val text = key.className("text")
    val more = key.className("more")
    val less = key.className("less")
    return "<style>${foldStyle(palette, key)}</style><div class=\"$fold\">" +
        "<details class=\"$toggle\"><summary class=\"${key.className("label")}\">" +
        "<span class=\"$more\">Show quoted text</span>" +
        "<span class=\"$less\">Hide quoted text</span></summary></details>" +
        "<div class=\"$body\">${quoteHeader(
            QuoteAuthor(quote.name, quote.initials, quote.date, quote.fullDate),
            key,
        )}" +
        "<div class=\"$text\">${sealedHtml(quote.html)}${deeperQuotes(quote.deeper, key)}</div></div></div>"
}

private fun foldStyle(
    palette: OtusPalette,
    key: RenderKey,
): String {
    val className = { name: String -> "." + key.className(name) }
    val open = "${className("toggle")}[open]"
    val foldedHeight = ReadTokens.quoteLineHeight * ReadTokens.QUOTE_FOLDED_LINES
    return listOf(
        cssRule(
            className("quote"),
            "display:flex",
            "flex-direction:column",
            "margin-top:${ReadTokens.quoteGap.css()}",
            "font-size:${ReadTokens.quoteTextSize.css()}",
            "line-height:${ReadTokens.quoteLineHeight.css()}",
            "color:${palette.text3.css()}",
        ),
        cssRule(className("toggle"), "order:2", "display:block"),
        cssRule(
            className("body"),
            "order:1",
            "border-left:${ReadTokens.quoteBorder.css()} solid ${palette.bgLight.css()}",
            "padding-left:${ReadTokens.quoteInset.css()}",
        ),
        cssRule(
            className("label"),
            "display:flex",
            "align-items:center",
            "min-height:${PanelTokens.touchTarget.css()}",
            "list-style:none",
            "color:${palette.text2.css()}",
            "-webkit-tap-highlight-color:transparent",
            "outline:none",
            "user-select:none",
        ),
        cssRule("${className("label")}::-webkit-details-marker", "display:none"),
        headerStyle(palette, className),
        cssRule(
            className("text"),
            "margin-top:${ReadTokens.quoteHeaderGap.css()}",
            "max-height:${foldedHeight.css()}",
            "overflow:hidden",
            "-webkit-mask-image:$QUOTE_FADE",
            "mask-image:$QUOTE_FADE",
        ),
        cssRule(
            "$open~${className("body")} ${className("text")}",
            "max-height:none",
            "-webkit-mask-image:none",
            "mask-image:none",
        ),
        cssRule(className("less"), "display:none"),
        cssRule("$open ${className("more")}", "display:none"),
        cssRule("$open ${className("less")}", "display:inline"),
    ).joinToString("")
}

/** The quote header's pill, with the author's initials in a circle. */
private fun headerStyle(
    palette: OtusPalette,
    className: (String) -> String,
): String =
    listOf(
        cssRule(
            className("pill"),
            "display:inline-flex",
            "align-items:center",
            "gap:${ReadTokens.glyphGap.css()}",
            "height:${ReadTokens.quotePillHeight.css()}",
            "box-sizing:border-box",
            "padding:0 ${PanelTokens.chipPadding.css()} 0 ${ReadTokens.quotePillStart.css()}",
            "border-radius:${(ReadTokens.quotePillHeight / 2).css()}",
            "background:${palette.bgDark.css()}",
            "border:${PanelTokens.borderWidth.css()} solid ${palette.bgLight.css()}",
            "color:${palette.text2.css()}",
            // One line, like the Compose pill: a long date ends in an ellipsis instead of wrapping.
            "white-space:nowrap",
            "max-width:100%",
            "overflow:hidden",
        ),
        cssRule(
            className("avatar"),
            "display:inline-flex",
            "align-items:center",
            "justify-content:center",
            "width:${ReadTokens.quoteAvatarSize.css()}",
            "height:${ReadTokens.quoteAvatarSize.css()}",
            "border-radius:${(ReadTokens.quoteAvatarSize / 2).css()}",
            "background:${palette.bgLight.css()}",
            "color:${palette.text1.css()}",
            "font-size:${ReadTokens.quoteInitialsSize.css()}",
            "flex-shrink:0",
        ),
        // The date follows the name inside the pill, after a line like a chip's divider.
        cssRule(
            className("divider"),
            "display:inline-block",
            "width:${PanelTokens.borderWidth.css()}",
            "height:${PanelTokens.chipDividerHeight.css()}",
            "margin:0 ${PanelTokens.iconGap.css()}",
            "background:${palette.bgLight.css()}",
        ),
        cssRule(
            className("date"),
            "color:${palette.text3.css()}",
            "min-width:0",
            "overflow:hidden",
            "text-overflow:ellipsis",
        ),
        dateToggleStyle(palette, className),
        deeperStyle(palette, className),
    ).joinToString("")

/** Who wrote a quote, by [name] and [initials], its short [date], and its [fullDate] with the time. */
internal class QuoteAuthor(
    val name: String,
    val initials: String,
    val date: String,
    val fullDate: String,
)

/**
 * Who wrote a quote and its date in one pill, divided by a line; the date alone without a name,
 * and the name alone without a date.
 */
internal fun quoteHeader(
    author: QuoteAuthor,
    key: RenderKey,
): String {
    val date = dateHtml(author, key).takeIf { author.date.isNotBlank() }
    val name = author.name.takeIf { it.isNotBlank() } ?: return date.orEmpty()
    val dated = date?.let { "<span class=\"${key.className("divider")}\"></span>$it" }.orEmpty()
    return "<span class=\"${key.className("pill")}\"><span class=\"${key.className("avatar")}\">" +
        "${escaped(author.initials)}</span>${escaped(name)}$dated</span>"
}

/**
 * The short date, or, when there is more to it, a `<details>` that shows the full date and time
 * after a tap on the chevron beside it, without scripts.
 */
private fun dateHtml(
    author: QuoteAuthor,
    key: RenderKey,
): String {
    val date = "<span class=\"${key.className("date")}\">${escaped(author.date)}</span>"
    if (author.fullDate.isBlank() || author.fullDate == author.date) return date
    return "<details class=\"${key.className("when")}\"><summary class=\"${key.className("when-label")}\">" +
        "<span class=\"${key.className("short")}\">$date</span>" +
        "<span class=\"${key.className("full")}\"><span class=\"${key.className("date")}\">" +
        "${escaped(author.fullDate)}</span></span>" +
        "<span class=\"${key.className("date-toggle")}\" aria-hidden=\"true\">›</span></summary></details>"
}

/** [text] safe to put between HTML tags. */
internal fun escaped(text: String): String =
    text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

private val QUOTE_FADE = "linear-gradient(#000 calc(100% * ${ReadTokens.QUOTE_FADE_START}),transparent)"
