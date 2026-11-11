package com.otusfoss.core.designsystem.mail.compose.elements

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.Dp

/** The images of a [RichText], each named by its `src`, and the one whose controls show. */
public class RichTextImages internal constructor(
    private val text: RichText,
) {
    /** The `src` of each image, in order, which a RichTextBody draws. */
    public val sources: List<String>
        get() = text.blocks.filterIsInstance<RichBlock.Image>().map { it.source }

    /** The `src` of the image whose controls show, or null. */
    public val selected: String? get() = text.selected?.source

    public fun deselect() {
        text.selected = null
    }

    /** The width of the image at [source], or null while it is as wide as the text. */
    public fun width(source: String): Dp? = image(source)?.width

    /** Sets the width of the image at [source]; null makes it as wide as the text. */
    public fun resize(
        source: String,
        width: Dp?,
    ) {
        image(source)?.width = width
    }

    /** The text that names the image at [source] to screen readers, its `alt`. */
    public fun description(source: String): String = image(source)?.description.orEmpty()

    public fun describe(
        source: String,
        description: String,
    ) {
        image(source)?.description = description
    }

    /** Puts the image at [source] at the cursor, splitting the text there; typing goes on after it. */
    public fun insert(source: String) {
        val blocks = text.blocks
        val block = text.active?.takeIf { it in blocks } ?: blocks.last() as RichBlock.Text
        block.state.insertHtmlAfterSelection(imageHtml(source))
        val parts = split(block.state.toHtml())
        val at = blocks.indexOf(block)
        blocks.removeAt(at)
        blocks.addAll(at, parts)
        val after = parts.last() as RichBlock.Text
        after.state.selection = TextRange(0)
        text.active = after
        text.focusNext = after
    }

    /** Shows the image at [new] where the one at [old] was, at its width and with its description. */
    public fun replace(
        old: String,
        new: String,
    ) {
        val at = text.blocks.indexOfFirst { it is RichBlock.Image && it.source == old }
        val image = text.blocks.getOrNull(at) as? RichBlock.Image ?: return
        val replacement = RichBlock.Image(new, image.width, image.description)
        text.blocks[at] = replacement
        if (text.selected === image) text.selected = replacement
    }

    /** Takes every image at [source] out of the text, joining the text around each one. */
    public fun remove(source: String) {
        text.blocks
            .withIndex()
            .filter { (_, block) -> block is RichBlock.Image && block.source == source }
            .reversed()
            .forEach { (index, _) -> text.join(index) }
    }

    /** Removes the image before the text block at [index], as Backspace at the block's start does. */
    internal fun removeBefore(index: Int) {
        if (text.blocks.getOrNull(index - 1) is RichBlock.Image) text.join(index - 1)
    }

    private fun image(source: String): RichBlock.Image? =
        text.blocks.firstOrNull { it is RichBlock.Image && it.source == source } as? RichBlock.Image
}
