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
            "<div class=\"${key.className("row")}\">${quoteHeader(level.name, level.initials, level.date, key)}</div>" +
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
