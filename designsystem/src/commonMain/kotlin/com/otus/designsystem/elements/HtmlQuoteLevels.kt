package com.otus.designsystem.elements

import com.otus.designsystem.theme.OtusPalette
import com.otus.designsystem.theme.ReadTokens

/** Plain [text] as HTML: escaped, with its line breaks kept. */
internal fun textHtml(text: String): String = escaped(text).replace("\n", "<br>")

/**
 * The [levels] a quote quotes, each whole under its pill, all at the same indent. They fold and
 * unfold with the quote around them, never on their own.
 */
internal fun deeperQuotes(
    levels: List<HtmlQuoteLevel>,
    key: RenderKey,
): String =
    levels.joinToString("") { level ->
        "<div class=\"${key.className("deeper")}\">" +
            "<div class=\"${key.className(
                "row",
            )}\">${quoteHeader(QuoteAuthor(level.name, level.initials, level.date, level.fullDate), key)}</div>" +
            "<div>${textHtml(level.text)}</div></div>"
    }

/** How the deeper levels look: a start line like the quote's, and a gap under each pill. */
internal fun deeperStyle(
    palette: OtusPalette,
    className: (String) -> String,
): String =
    listOf(
        cssRule(
            className("deeper"),
            "margin-top:${ReadTokens.quoteHeaderGap.css()}",
            "border-left:${ReadTokens.quoteBorder.css()} solid ${palette.bgLight.css()}",
            "padding-left:${ReadTokens.quoteInset.css()}",
        ),
        cssRule(className("row"), "margin-bottom:${ReadTokens.quoteHeaderGap.css()}"),
    ).joinToString("")

/** The date's chevron: hidden full date, pointing on while short and back once the full date shows. */
internal fun dateToggleStyle(
    palette: OtusPalette,
    className: (String) -> String,
): String {
    val open = "${className("when")}[open]"
    return listOf(
        cssRule(className("when"), "display:inline", "min-width:0"),
        cssRule(
            className("when-label"),
            "display:inline-flex",
            "align-items:center",
            "gap:${ReadTokens.glyphGap.css()}",
            "list-style:none",
            "cursor:pointer",
            // No browser tap box or focus ring; the date and chevron changing are the feedback.
            "-webkit-tap-highlight-color:transparent",
            "outline:none",
            "user-select:none",
        ),
        cssRule("${className("when-label")}::-webkit-details-marker", "display:none"),
        cssRule(className("full"), "display:none"),
        cssRule("$open ${className("full")}", "display:inline"),
        cssRule("$open ${className("short")}", "display:none"),
        cssRule(className("date-toggle"), "display:inline-block", "color:${palette.text3.css()}"),
        cssRule("$open ${className("date-toggle")}", "transform:rotate(180deg)"),
    ).joinToString("")
}
