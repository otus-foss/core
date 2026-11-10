package com.otus.designsystem.mail.compose.elements

import androidx.compose.ui.text.TextRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RichTextLinkTest {
    @Test
    fun linkingASelectionWithEdgeSpacesKeepsTheSpacesOutsideTheLink() {
        val text = richText("<p>Click <b>here </b>now</p>", TextRange(6, 11))
        text.addLink("https://example.com", "here")
        assertEquals("Click here now", text.text)
        assertTrue("<b>here</b></a><b> </b>now" in text.html, text.html)
    }

    @Test
    fun linkingKeepsTheSelectionsFormatting() {
        val text = richText("<p>Click <b>here</b> now</p>", TextRange(5, 10))
        text.addLink("https://example.com", " here ")
        assertEquals("Click here now", text.text)
        assertTrue("<a href=\"https://example.com\" target=\"_blank\"><b>here</b></a> now" in text.html, text.html)
    }

    @Test
    fun newTextReplacesTheSelectionButNotItsEdgeSpaces() {
        val text = richText("<p>Click here now</p>", TextRange(5, 11))
        text.addLink("https://example.com", "the docs")
        assertEquals("Click the docs now", text.text)
    }

    @Test
    fun aSelectionOfOnlySpacesShrinksToACaretAfterIt() {
        assertEquals(TextRange(3), TextRange(1, 3).withoutEdgeSpaces("a  b"))
    }

    private fun richText(
        html: String,
        selection: TextRange,
    ): RichText =
        RichText(html).apply {
            (blocks.first() as RichBlock.Text).state.selection = selection
        }
}
