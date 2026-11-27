package com.otusfoss.core.designsystem.mail.compose.elements

import androidx.compose.ui.text.TextRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RichTextClearTest {
    @Test
    fun clearingASelectionRemovesFormatsThatCoverOnlyPartOfIt() {
        val text = richText("<p>Hello <b>world</b> and <i>more</i></p>", TextRange(0, 20))
        text.toggle(TextFormat.Clear)
        assertEquals("Hello world and more", text.text)
        assertFalse("<b>" in text.html || "<i>" in text.html, text.html)
    }

    @Test
    fun clearingASelectionRemovesLinksInsideIt() {
        val text = richText("<p>Go <a href=\"https://example.com\">here</a> now</p>", TextRange(0, 11))
        text.toggle(TextFormat.Clear)
        assertFalse("<a " in text.html, text.html)
    }
}

/** A [RichText] of [html] with [selection] in its first text block. */
internal fun richText(
    html: String,
    selection: TextRange,
): RichText =
    RichText(html).apply {
        (blocks.first() as RichBlock.Text).state.selection = selection
    }
