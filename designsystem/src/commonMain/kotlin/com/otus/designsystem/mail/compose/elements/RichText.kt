package com.otus.designsystem.mail.compose.elements

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlin.math.roundToInt

/** A formatting the [FormattingBar][com.otus.designsystem.mail.compose.components.FormattingBar] turns on or off. */
public enum class TextFormat { Bold, Italic, Bullets, Numbers, Link, Clear }

/**
 * Formatted text being written, such as a message body or a signature. It reads and writes
 * [html], and [text] is the same content without formatting or images, for a plain-text part.
 * Reading either inside a snapshot observer, such as `snapshotFlow`, follows every edit.
 *
 * A text field cannot draw images, so the text is a run of [RichBlock]s: text, then each image
 * with the text after it; [images] edits them. Formatting applies to the text block that last had
 * focus.
 */
@Stable
public class RichText(
    html: String = "",
) {
    internal val blocks = mutableStateListOf<RichBlock>()

    /** The text block formatting and new images go to. */
    internal var active: RichBlock.Text? by mutableStateOf(null)

    /** The image showing its controls, if any; focusing text deselects it. */
    internal var selected: RichBlock.Image? by mutableStateOf(null)

    /** How wide the text is drawn, which a full-width image matches. */
    internal var textWidth: Dp by mutableStateOf(0.dp)

    /** The images between the text, by their `src`. */
    public val images: RichTextImages = RichTextImages(this)

    /** A text block to focus next, with the cursor where [RichBlock.Text.state] has it. */
    internal var focusNext: RichBlock.Text? by mutableStateOf(null)

    init {
        setHtml(html)
    }

    public val html: String
        get() = if (blocks.size == 1 && text.isEmpty()) "" else blocks.joinToString("") { it.html }

    public val text: String
        get() =
            blocks
                .filterIsInstance<RichBlock.Text>()
                .map { it.state.annotatedString.text }
                .filter { it.isNotEmpty() }
                .joinToString("\n")

    private val current: RichTextState
        get() = (active?.takeIf { it in blocks } ?: blocks.first() as RichBlock.Text).state

    public fun setHtml(html: String) {
        blocks.clear()
        blocks += split(html)
        active = null
    }

    /** Whether [format] applies at the cursor, such as bold text or a bulleted line. */
    public fun isActive(format: TextFormat): Boolean =
        when (format) {
            TextFormat.Bold -> current.currentSpanStyle.fontWeight == FontWeight.Bold
            TextFormat.Italic -> current.currentSpanStyle.fontStyle == FontStyle.Italic
            TextFormat.Bullets -> current.isUnorderedList
            TextFormat.Numbers -> current.isOrderedList
            TextFormat.Link -> current.isLink
            TextFormat.Clear -> false
        }

    /** Turns [format] on or off at the cursor. A link needs [addLink]; here it only removes one. */
    public fun toggle(format: TextFormat) {
        val state = current
        when (format) {
            TextFormat.Bold -> state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
            TextFormat.Italic -> state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
            TextFormat.Bullets -> state.keepingTypingStyle { toggleUnorderedList() }
            TextFormat.Numbers -> state.keepingTypingStyle { toggleOrderedList() }
            TextFormat.Link -> if (state.isLink) state.removeLink()
            TextFormat.Clear -> state.clearFormatting()
        }
    }

    /** The text selected in the block being written, empty while the cursor is a caret. */
    public val selectedText: String
        get() = current.run { annotatedString.text.substring(selection.min, selection.max) }

    /**
     * Adds a link to [url] that reads [text], or [url] itself while [text] is blank. Selected text
     * that already reads so keeps its formatting; other selected text is replaced. Spaces at the
     * ends of the selection stay outside the link.
     */
    public fun addLink(
        url: String,
        text: String = selectedText,
    ) {
        val state = current
        state.selection = state.selection.withoutEdgeSpaces(state.annotatedString.text)
        val shown = text.trim().ifBlank { url }
        if (shown == selectedText) {
            state.addLinkToSelection(url)
        } else {
            state.removeSelectedText()
            state.addLink(shown, url)
        }
    }

    /** Removes the image at [index] and joins the text blocks around it, the cursor where they meet. */
    internal fun join(index: Int) {
        val before = blocks[index - 1] as RichBlock.Text
        val after = blocks[index + 1] as RichBlock.Text
        val joined = RichBlock.Text(before.html + after.html)
        joined.state.selection = TextRange(before.state.annotatedString.text.length)
        if (selected === blocks[index]) selected = null
        blocks.removeRange(index - 1, index + 2)
        blocks.add(index - 1, joined)
        active = joined
        focusNext = joined
    }
}

/** This range inside [text], without the spaces at its ends. */
internal fun TextRange.withoutEdgeSpaces(text: String): TextRange {
    val part = text.substring(min, max)
    val start = min + part.length - part.trimStart().length
    return TextRange(start, maxOf(start, min + part.trimEnd().length))
}

/**
 * Runs [change] and turns the styles at the cursor back on. A list rewrites its line, and the
 * editor then drops styles that were turned on for text not typed yet, such as Bold on an empty line.
 */
private fun RichTextState.keepingTypingStyle(change: RichTextState.() -> Unit) {
    val style = currentSpanStyle
    change()
    addSpanStyle(style)
}

/** Removes every span style, link, and list at the cursor. */
private fun RichTextState.clearFormatting() {
    clearSpanStyles()
    clearRichSpans()
    if (isUnorderedList) removeUnorderedList()
    if (isOrderedList) removeOrderedList()
}

/** One part of a [RichText]: a run of formatted text, or an image the text names by its `src`. */
internal sealed interface RichBlock {
    val html: String

    class Text(
        html: String,
    ) : RichBlock {
        val state: RichTextState = RichTextState().apply { if (html.isNotBlank()) setHtml(html) }

        override val html: String
            get() = if (state.annotatedString.text.isEmpty()) "" else state.toHtml()
    }

    /** An image at [width], or as wide as the text while that is null, named by its [description]. */
    class Image(
        val source: String,
        width: Dp? = null,
        description: String = "",
    ) : RichBlock {
        var width: Dp? by mutableStateOf(width)
        var description: String by mutableStateOf(description)

        override val html: String get() = imageHtml(source, width, description)
    }
}

/** [html] as text blocks with an image block between each two, always starting and ending in text. */
internal fun split(html: String): List<RichBlock> {
    val images = imageTags(html)
    return (listOf(0) + images.map { it.end })
        .zip(images.map { it.start } + html.length)
        .map { (end, start) -> RichBlock.Text(html.substring(end, start)) }
        .zip(images.map { RichBlock.Image(it.source, it.width?.dp, it.description) } + null)
        .flatMap { (text, image) -> listOfNotNull(text, image) }
}

/** An image tag that never grows wider than the message it is in. */
internal fun imageHtml(
    source: String,
    width: Dp? = null,
    description: String = "",
): String {
    val sized = width?.let { " width=\"${it.value.roundToInt()}\"" }.orEmpty()
    val alt = description.takeIf { it.isNotBlank() }?.let { " alt=\"${it.escaped()}\"" }.orEmpty()
    return "<img src=\"$source\"$sized$alt style=\"max-width:100%\">"
}

private fun String.escaped(): String = replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;")
