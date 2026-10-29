package com.otus.designsystem.mail.read.elements

import com.otus.designsystem.theme.colors.OtusPalette
import com.otus.designsystem.theme.tokens.content.ReadTokens

/**
 * Plain [text] as HTML: escaped, with its line breaks kept, and its links as links, as the body
 * shows them: a Markdown link such as `[Proton Mail](https://proton.me)` by its label, and a bare
 * web address as itself.
 */
internal fun textHtml(text: String): String {
    // A plain loop: the text between links is escaped piece by piece, and each piece needs the last end.
    val html =
        buildString {
            var last = 0
            LINK.findAll(text).forEach { link ->
                val (label, target, bare) = link.destructured
                val url = target.ifEmpty { bare }
                append(escaped(text.substring(last, link.range.first)))
                append("<a href=\"${escaped(url)}\">${escaped(label.ifEmpty { url })}</a>")
                last = link.range.last + 1
            }
            append(escaped(text.substring(last)))
        }
    return html.replace("\n", "<br>")
}

/** A Markdown link to a web or mail address, or a bare web address without trailing punctuation. */
private val LINK =
    Regex("""\[([^\]\n]+)]\(((?:https?://|mailto:)[^\s()<>]+)\)|(https?://[^\s()<>]*[^\s()<>.,;:!?])""")

/**
 * The [levels] a quote quotes, each whole under its pill and inside the one before it, one step
 * further in for every level. They fold and unfold with the quote around them, never on their own.
 */
internal fun deeperQuotes(
    levels: List<HtmlQuoteLevel>,
    key: RenderKey,
): String =
    levels.foldRight("") { level, inner ->
        val author = QuoteAuthor(level.name, level.initials, level.date, level.fullDate)
        "<div class=\"${key.className("deeper")}\">" +
            "<div class=\"${key.className("row")}\">${quoteHeader(author, key)}</div>" +
            "<div>${textHtml(level.text)}</div>$inner</div>"
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
