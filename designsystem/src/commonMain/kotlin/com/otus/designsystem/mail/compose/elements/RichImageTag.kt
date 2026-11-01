package com.otus.designsystem.mail.compose.elements

import com.otus.designsystem.mail.read.elements.scanHtml

/**
 * An `<img>` start tag in a [RichText]'s HTML, from [start] to [end], past its `>`, with its
 * [source], its [width] in pixels when it gives a whole number, and its alt text as [description].
 */
internal class RichImageTag(
    val start: Int,
    val end: Int,
    val source: String,
    val width: Int?,
    val description: String,
)

/**
 * Every `<img>` with a `src` in [html], in order. One linear scan finds them, so a draft that
 * quotes a sender's markup, such as many `<img` without a `>`, cannot stall the editor.
 */
internal fun imageTags(html: String): List<RichImageTag> =
    scanHtml(html)
        .tags
        .filter { it.name == "img" && !it.closing && "src" in it.attributes }
        .map { tag ->
            RichImageTag(
                start = tag.start,
                end = tag.end,
                source = tag.attributes.getValue("src"),
                width = tag.attributes["width"]?.toIntOrNull(),
                description = tag.attributes["alt"].orEmpty().unescaped(),
            )
        }

/** Undoes the escapes [imageHtml] writes into an alt text. */
private fun String.unescaped(): String = replace("&lt;", "<").replace("&quot;", "\"").replace("&amp;", "&")
