package com.otus.designsystem.elements

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.mohamedrejeb.richeditor.model.RichTextState

/** A formatting the [FormattingBar][com.otus.designsystem.components.FormattingBar] turns on or off. */
public enum class TextFormat { Bold, Italic, Bullets, Numbers, Link, Clear }

/**
 * Formatted text being written, such as a message body or a signature. It reads and writes
 * [html], and [text] is the same content without formatting or images, for a plain-text part.
 * Reading either inside a snapshot observer, such as `snapshotFlow`, follows every edit.
 *
 * A text field cannot draw images, so the text is a run of [RichBlock]s: text, then each image
 * with the text after it. Formatting applies to the text block that last had focus.
 */
@Stable
public class RichText(
    html: String = "",
) {
    internal val blocks = mutableStateListOf<RichBlock>()

    /** The text block formatting and new images go to. */
    internal var active: RichBlock.Text? by mutableStateOf(null)

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

    /** The `src` of each image in the text, in order; a [RichTextBody] draws them with its images. */
    public val imageSources: List<String>
        get() = blocks.filterIsInstance<RichBlock.Image>().map { it.source }

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
            TextFormat.Bold -> {
                state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
            }

            TextFormat.Italic -> {
                state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
            }

            TextFormat.Bullets -> {
                state.toggleUnorderedList()
            }

            TextFormat.Numbers -> {
                state.toggleOrderedList()
            }

            TextFormat.Link -> {
                if (state.isLink) state.removeLink()
            }

            TextFormat.Clear -> {
                state.clearSpanStyles()
                state.clearRichSpans()
                if (state.isUnorderedList) state.removeUnorderedList()
                if (state.isOrderedList) state.removeOrderedList()
            }
        }
    }

    /** Links the selected text to [url], or inserts [url] as its own text when nothing is selected. */
    public fun addLink(url: String) {
        val state = current
        if (state.selection.collapsed) state.addLink(url, url) else state.addLinkToSelection(url)
    }

    /** Puts the image at [source] at the cursor, splitting the text there; typing goes on after it. */
    public fun insertImage(source: String) {
        val block = active?.takeIf { it in blocks } ?: blocks.last() as RichBlock.Text
        block.state.insertHtmlAfterSelection(imageHtml(source))
        val parts = split(block.state.toHtml())
        val at = blocks.indexOf(block)
        blocks.removeAt(at)
        blocks.addAll(at, parts)
        val after = parts.last() as RichBlock.Text
        after.state.selection = TextRange(0)
        active = after
        focusNext = after
    }

    /** Takes every image at [source] out of the text, joining the text around each one. */
    public fun removeImage(source: String) {
        blocks
            .withIndex()
            .filter { (_, block) -> block is RichBlock.Image && block.source == source }
            .reversed()
            .forEach { (index, _) -> join(index) }
    }

    /** Removes the image before the text block at [index], as Backspace at the block's start does. */
    internal fun removeImageBefore(index: Int) {
        if (blocks.getOrNull(index - 1) is RichBlock.Image) join(index - 1)
    }

    /** Removes the image at [index] and joins the text blocks around it, the cursor where they meet. */
    private fun join(index: Int) {
        val before = blocks[index - 1] as RichBlock.Text
        val after = blocks[index + 1] as RichBlock.Text
        val joined = RichBlock.Text(before.html + after.html)
        joined.state.selection = TextRange(before.state.annotatedString.text.length)
        blocks.removeRange(index - 1, index + 2)
        blocks.add(index - 1, joined)
        active = joined
        focusNext = joined
    }
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

    class Image(
        val source: String,
    ) : RichBlock {
        override val html: String get() = imageHtml(source)
    }
}

/** [html] as text blocks with an image block between each two, always starting and ending in text. */
private fun split(html: String): List<RichBlock> {
    val images = imageTag.findAll(html).toList()
    val texts =
        (listOf(-1) + images.map { it.range.last })
            .zip(images.map { it.range.first } + html.length)
            .map { (end, start) -> RichBlock.Text(html.substring(end + 1, start)) }
    return texts
        .zip(images.map { RichBlock.Image(it.groupValues[1]) } + null)
        .flatMap { (text, image) -> listOfNotNull(text, image) }
}

private fun imageHtml(source: String) = "<img src=\"$source\">"

private val imageTag = Regex("""<img\b[^>]*\bsrc="([^"]*)"[^>]*>""")
