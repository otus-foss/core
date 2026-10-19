package com.otus.designsystem.elements

import com.otus.designsystem.theme.OtusPalette
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.ReadTokens

/** Plain [text] as HTML: escaped, with its line breaks kept. */
internal fun textHtml(text: String): String = escaped(text).replace("\n", "<br>")

/**
 * The [levels] a quote quotes, as one folded row each: a `<details>` whose summary is the level's
 * pill and a chevron, and whose body is the level's own text. They all sit at the same indent.
 */
internal fun deeperQuotes(
    levels: List<HtmlQuoteLevel>,
    key: RenderKey,
): String =
    levels.joinToString("") { level ->
        "<details class=\"${key.className("deeper")}\"><summary class=\"${key.className("row")}\">" +
            quoteHeader(level.name, level.initials, level.date, key) +
            "<span class=\"${key.className("chevron")}\"></span></summary>" +
            "<div class=\"${key.className("deeper-text")}\">${textHtml(level.text)}</div></details>"
    }

/** How the deeper rows look: a start line like the quote's, a full touch target, and a turning chevron. */
internal fun deeperStyle(
    palette: OtusPalette,
    className: (String) -> String,
): String {
    val chevron = ReadTokens.quoteChevronSize.css()
    return listOf(
        cssRule(
            className("deeper"),
            "margin-top:${ReadTokens.quoteHeaderGap.css()}",
            "border-left:${ReadTokens.quoteBorder.css()} solid ${palette.bgLight.css()}",
            "padding-left:${ReadTokens.quoteInset.css()}",
        ),
        cssRule(
            className("row"),
            "display:flex",
            "align-items:center",
            "min-height:${PanelTokens.touchTarget.css()}",
            "list-style:none",
        ),
        cssRule("${className("row")}::-webkit-details-marker", "display:none"),
        cssRule(
            className("chevron"),
            "margin-left:auto",
            "width:$chevron",
            "height:$chevron",
            "border-right:${PanelTokens.borderWidth.css()} solid ${palette.text2.css()}",
            "border-bottom:${PanelTokens.borderWidth.css()} solid ${palette.text2.css()}",
            "transform:rotate(45deg)",
        ),
        cssRule("${className("deeper")}[open] ${className("chevron")}", "transform:rotate(-135deg)"),
    ).joinToString("")
}
