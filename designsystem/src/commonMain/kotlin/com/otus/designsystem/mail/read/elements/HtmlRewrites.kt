package com.otus.designsystem.mail.read.elements

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.otus.designsystem.theme.colors.OtusPalette
import com.otus.designsystem.theme.tokens.content.ReadTokens

/**
 * [html] with each remote `<img>` swapped for a dashed placeholder of the same size, so the layout
 * holds while images stay blocked. A placeholder links to the [key]'s show images link. Images with
 * a `data:` or `cid:` source are part of the message and stay. Only real start tags count, found
 * in one linear scan, so text, comments, and style sheets stay as they are.
 */
internal fun withImagePlaceholders(
    html: String,
    key: RenderKey,
): String {
    val remote = scanHtml(html).tags.filter { it.name == "img" && !it.closing && isRemote(it) }
    if (remote.isEmpty()) return html
    return buildString {
        // A plain loop: the markup between images is copied piece by piece, and each piece needs the last end.
        var last = 0
        remote.forEach { image ->
            appendRange(html, last, image.start)
            append(placeholder(image.text, key))
            last = image.end
        }
        appendRange(html, last, html.length)
    }
}

/** Whether the image [tag]'s `src` is a web address, which a placeholder stands in for. */
private fun isRemote(tag: HtmlTag): Boolean = REMOTE.containsMatchIn(tag.attributes["src"].orEmpty())

/** The show images link that stands in for the image [tag]. */
private fun placeholder(
    tag: String,
    key: RenderKey,
): String =
    "<a href=\"${key.showImagesUrl}\" class=\"${key.className(BLOCKED)}\" style=\"${placeholderSize(tag)}\" " +
        "aria-label=\"Show images\"></a>"

/**
 * The placeholder's size from the image's own. With both sides in pixels it keeps their ratio and
 * still shrinks to the page width; a missing side falls back to the full width or the token height.
 */
private fun placeholderSize(tag: String): String {
    val width = dimension(tag, "width")
    val height = dimension(tag, "height")
    return if (width?.unit == PX && height?.unit == PX) {
        "width:${width.css};max-width:100%;aspect-ratio:${width.number}/${height.number};height:auto"
    } else {
        "width:${width?.css ?: "100%"};height:${height?.css ?: ReadTokens.placeholderHeight.css()}"
    }
}

/** A size an image gives, as a number and `px` or `%`. */
private class Dimension(
    val number: String,
    val unit: String,
) {
    val css: String get() = number + unit
}

/** The size [name] the tag gives in an attribute, where a bare number means pixels, or else in its inline style. */
private fun dimension(
    tag: String,
    name: String,
): Dimension? {
    val match =
        Regex("""(?i)\s$name\s*=\s*["']?(\d+(?:\.\d+)?)(%|px)?""").find(tag)
            ?: Regex("""(?i)[\s;"']$name\s*:\s*(\d+(?:\.\d+)?)(px|%)""").find(tag)
    return match?.let { Dimension(it.groupValues[1], it.groupValues[2].lowercase().ifEmpty { PX }) }
}

/** The style of the placeholders: a dashed `text3` border around a crossed-out image mark. */
internal fun placeholderStyle(
    palette: OtusPalette,
    key: RenderKey,
): String {
    val color = palette.text3.css()
    val mark =
        "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 -960 960 960'%3E" +
            "%3Cpath fill='${color.replace("#", "%23")}' d='$HIDE_IMAGE_PATH'/%3E%3C/svg%3E"
    return cssRule(
        ".${key.className(BLOCKED)}",
        "display:inline-block",
        "box-sizing:border-box",
        "max-width:100%",
        "vertical-align:middle",
        "border:${ReadTokens.placeholderBorder.css()} dashed $color",
        "border-radius:${ReadTokens.placeholderRadius.css()}",
        "background:url(\"$mark\") center/${ReadTokens.placeholderIcon.css()} no-repeat",
    )
}

/** A CSS rule whose [declarations] win over the message's own style sheets. */
internal fun cssRule(
    selector: String,
    vararg declarations: String,
): String = declarations.joinToString(";", "$selector{", "}") { "$it!important" }

/** A size in CSS pixels, which match density-independent pixels at the view's default zoom. */
internal fun Dp.css(): String = value.cssNumber() + PX

internal fun TextUnit.css(): String = value.cssNumber() + PX

private fun Float.cssNumber(): String = if (this % 1f == 0f) toInt().toString() else toString()

private const val PX = "px"
private const val BLOCKED = "blocked"

private val REMOTE = Regex("""(?i)^\s*(?:https?:)?//""")

/** The Material Symbols `hide_image` path, drawn in the placeholder's center. */
private const val HIDE_IMAGE_PATH =
    "M840-760v440q0 20-12.5 30T800-280q-15 0-27.5-10.5T760-321v-439H320q-20 0-30-12.5T280-800q0-15 " +
        "10-27.5t30-12.5h440q33 0 56.5 23.5T840-760ZM200-120q-33 0-56.5-23.5T120-200v-528l-36-36q-11-11-11-28t11-28" +
        "q11-11 28-11t28 11l680 680q11 11 11 28t-11 28q-11 11-28 11t-28-11l-36-36H200Zm367-160H280q-12 0-18-11t2-21" +
        "l80-107q6-8 16-8t16 8l74 99 33-44-283-283v447h447l-80-80Zm-30-257ZM424-424Z"
