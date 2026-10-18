package com.otus.designsystem.elements

import com.otus.designsystem.theme.OtusPalette

/** The link a blocked image placeholder opens; [HtmlView] catches it and asks to show images. */
internal const val SHOW_IMAGES_URL = "otus-images:show"

/**
 * [html] with each remote `<img>` swapped for a dashed placeholder of the same size, so the layout
 * holds while images stay blocked. A placeholder is a link to [SHOW_IMAGES_URL]. Images with a
 * `data:` or `cid:` source are part of the message and stay.
 */
internal fun withImagePlaceholders(html: String): String =
    IMG_TAG.replace(html) { match ->
        val tag = match.value
        val source =
            SRC
                .find(tag)
                ?.groupValues
                ?.drop(1)
                ?.firstOrNull { it.isNotEmpty() }
                .orEmpty()
        if (!REMOTE.containsMatchIn(source)) {
            tag
        } else {
            val width = dimension(tag, "width")?.let { "${it}px" } ?: "100%"
            val height = dimension(tag, "height")?.let { "${it}px" } ?: "${PLACEHOLDER_HEIGHT}px"
            "<a href=\"$SHOW_IMAGES_URL\" class=\"otus-blocked\" style=\"width:$width;height:$height\" " +
                "aria-label=\"Show images\"></a>"
        }
    }

/** The pixel size [name] the tag gives in an attribute, or else in its inline style. */
private fun dimension(
    tag: String,
    name: String,
): String? =
    Regex("""(?i)\s$name\s*=\s*["']?(\d+)""").find(tag)?.groupValues?.get(1)
        ?: Regex("""(?i)[\s;"']$name\s*:\s*(\d+)px""").find(tag)?.groupValues?.get(1)

/** The style of the placeholders: a dashed `text3` border around a crossed-out image mark. */
internal fun placeholderStyle(palette: OtusPalette): String {
    val color = palette.text3.css()
    val mark =
        "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 -960 960 960'%3E" +
            "%3Cpath fill='${color.replace("#", "%23")}' d='$HIDE_IMAGE_PATH'/%3E%3C/svg%3E"
    return ".otus-blocked{display:inline-block;box-sizing:border-box;max-width:100%;vertical-align:middle;" +
        "border:2px dashed $color;border-radius:14px;background:url(\"$mark\") center/24px no-repeat}"
}

/**
 * The fold for [quote] after the body: a header pill with the author's initials and name and the
 * date, the quote cut to three lines that fade out, and a label that unfolds it. A hidden checkbox
 * holds the state, so it works without scripts.
 */
internal fun quoteFold(
    quote: HtmlQuote,
    palette: OtusPalette,
): String {
    val style =
        ".otus-quote{margin-top:16px;font-size:14px;line-height:21px;color:${palette.text3.css()}}" +
            ".otus-quote-toggle{display:none}" +
            ".otus-quote-body{border-left:2px solid ${palette.bgLight.css()};padding-left:12px}" +
            ".otus-quote-pill{display:inline-flex;align-items:center;gap:6px;height:26px;box-sizing:border-box;" +
            "padding:0 10px 0 3px;border-radius:13px;background:${palette.bgDark.css()};" +
            "border:1px solid ${palette.bgLight.css()};color:${palette.text2.css()}}" +
            ".otus-quote-avatar{display:inline-flex;align-items:center;justify-content:center;width:20px;" +
            "height:20px;border-radius:10px;background:${palette.bgLight.css()};color:${palette.text1.css()};" +
            "font-size:9px}" +
            ".otus-quote-text{margin-top:8px;max-height:63px;overflow:hidden;" +
            "-webkit-mask-image:$QUOTE_FADE;mask-image:$QUOTE_FADE}" +
            ".otus-quote-toggle:checked~.otus-quote-body .otus-quote-text{max-height:none;" +
            "-webkit-mask-image:none;mask-image:none}" +
            ".otus-quote-label{display:block;padding:14px 0;color:${palette.text2.css()}}" +
            ".otus-quote-less,.otus-quote-toggle:checked~.otus-quote-label .otus-quote-more{display:none}" +
            ".otus-quote-toggle:checked~.otus-quote-label .otus-quote-less{display:inline}"
    return "<style>$style</style><div class=\"otus-quote\">" +
        "<input type=\"checkbox\" id=\"otus-quote\" class=\"otus-quote-toggle\">" +
        "<div class=\"otus-quote-body\"><span class=\"otus-quote-pill\">" +
        "<span class=\"otus-quote-avatar\">${escaped(quote.initials)}</span>${escaped(quote.name)}</span>" +
        " · ${escaped(quote.date)}<div class=\"otus-quote-text\">${quote.html}</div></div>" +
        "<label for=\"otus-quote\" class=\"otus-quote-label\"><span class=\"otus-quote-more\">Show quoted text</span>" +
        "<span class=\"otus-quote-less\">Hide quoted text</span></label></div>"
}

/** [text] safe to put between HTML tags. */
private fun escaped(text: String): String =
    text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

private val IMG_TAG = Regex("""(?i)<img\b[^>]*>""")
private val SRC = Regex("""(?i)\ssrc\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+))""")
private val REMOTE = Regex("""(?i)^\s*(?:https?:)?//""")
private const val PLACEHOLDER_HEIGHT = 120
private const val QUOTE_FADE = "linear-gradient(#000 30%,transparent)"

/** The Material Symbols `hide_image` path, drawn in the placeholder's center. */
private const val HIDE_IMAGE_PATH =
    "M840-760v440q0 20-12.5 30T800-280q-15 0-27.5-10.5T760-321v-439H320q-20 0-30-12.5T280-800q0-15 " +
        "10-27.5t30-12.5h440q33 0 56.5 23.5T840-760ZM200-120q-33 0-56.5-23.5T120-200v-528l-36-36q-11-11-11-28t11-28" +
        "q11-11 28-11t28 11l680 680q11 11 11 28t-11 28q-11 11-28 11t-28-11l-36-36H200Zm367-160H280q-12 0-18-11t2-21" +
        "l80-107q6-8 16-8t16 8l74 99 33-44-283-283v447h447l-80-80Zm-30-257ZM424-424Z"
