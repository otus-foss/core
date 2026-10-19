package com.otus.designsystem.elements

import com.otus.designsystem.theme.OtusPalette
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.ReadTokens

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
        "<div class=\"$body\">${quoteHeader(quote, key)}" +
        "<div class=\"$text\">${sealedHtml(quote.html)}</div></div></div>"
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
        ),
    ).joinToString("")

/** The pill of who wrote [quote], left out without a name, then its date when it has one. */
private fun quoteHeader(
    quote: HtmlQuote,
    key: RenderKey,
): String {
    val pill =
        quote.name.takeIf { it.isNotBlank() }?.let {
            "<span class=\"${key.className("pill")}\"><span class=\"${key.className("avatar")}\">" +
                "${escaped(quote.initials)}</span>${escaped(it)}</span>"
        }
    val date = escaped(quote.date).takeIf { it.isNotBlank() }
    return listOfNotNull(pill, date).joinToString(" · ")
}

/** [text] safe to put between HTML tags. */
private fun escaped(text: String): String =
    text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

private val QUOTE_FADE = "linear-gradient(#000 calc(100% * ${ReadTokens.QUOTE_FADE_START}),transparent)"
