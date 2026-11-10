package com.otus.designsystem.mail.compose.elements

import androidx.compose.ui.text.TextRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RichTextLinkTest {
    @Test
    fun linkingASelectionWithEdgeSpacesKeepsTheSpacesOutsideTheLink() {
        val text = richText("<p>Click <b>here </b>now</p>", TextRange(6, 11))
        text.addLink(URL, "here")
        assertEquals("Click here now", text.text)
        assertTrue("<b>here</b></a><b> </b>now" in text.html, text.html)
    }

    @Test
    fun linkingKeepsTheSelectionsFormatting() {
        val text = richText("<p>Click <b>here</b> now</p>", TextRange(5, 10))
        text.addLink(URL, " here ")
        assertEquals("Click here now", text.text)
        assertTrue("$LINK<b>here</b></a> now" in text.html, text.html)
    }

    @Test
    fun theDefaultTextLinksTheSelectionWithoutItsEdgeSpaces() {
        val text = richText("<p>Click <b>here </b>now</p>", TextRange(6, 11))
        text.addLink(URL)
        assertEquals("Click here now", text.text)
        assertTrue("$LINK<b>here</b></a><b> </b>now" in text.html, text.html)
    }

    @Test
    fun aReversedSelectionLinksTheSameText() {
        val text = richText("<p>Click <b>here </b>now</p>", TextRange(11, 6))
        text.addLink(URL, "here")
        assertEquals("Click here now", text.text)
        assertTrue("$LINK<b>here</b></a><b> </b>now" in text.html, text.html)
    }

    @Test
    fun newTextReplacesTheSelectionButNotItsEdgeSpaces() {
        val text = richText("<p>Click here now</p>", TextRange(5, 11))
        text.addLink(URL, "the docs")
        assertEquals("Click the docs now", text.text)
    }

    @Test
    fun aCaretInsertsTheLinkThere() {
        val text = richText("<p>Click now</p>", TextRange(6))
        text.addLink(URL, "docs")
        assertEquals("Click docsnow", text.text)
        assertTrue("${LINK}docs</a>" in text.html, text.html)
    }

    @Test
    fun aSelectionOfOnlyASpaceKeepsItAndTheLinkGoesAfter() {
        val text = richText("<p>Click now</p>", TextRange(5, 6))
        text.addLink(URL, "docs")
        assertEquals("Click docsnow", text.text)
    }

    @Test
    fun aSelectionEndingOnAParagraphBreakLinksOnlyTheText() {
        val text = richText("<p>a</p><p>b</p>", TextRange(0, 2))
        text.addLink(URL, "a")
        assertTrue("${LINK}a</a></p><p>b</p>" in text.html, text.html)
    }

    @Test
    fun edgeSpacesComeOffBothEndsAndKeepTheDirection() {
        assertEquals(TextRange(6, 10), TextRange(5, 11).withoutEdgeSpaces("Click here now"))
        assertEquals(TextRange(10, 6), TextRange(11, 5).withoutEdgeSpaces("Click here now"))
        assertEquals(TextRange(3), TextRange(1, 3).withoutEdgeSpaces("a  b"))
    }

    private fun richText(
        html: String,
        selection: TextRange,
    ): RichText =
        RichText(html).apply {
            (blocks.first() as RichBlock.Text).state.selection = selection
        }

    private companion object {
        const val URL = "https://example.com"
        const val LINK = "<a href=\"$URL\" target=\"_blank\">"
    }
}
