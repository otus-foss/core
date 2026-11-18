package com.otusfoss.core.designsystem.mail.read.html

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HtmlInertTest {
    @Test
    fun aSrcdocFrameBecomesInertInAnySpelling() {
        listOf("<iframe ", "<IFRAME ", "<IfRaMe\t", "<iframe\n").forEach { start ->
            val inert = inertFramesAndForms("${start}srcdoc=\"$PRECONNECT\"></iframe><p>After</p>")
            assertFalse(FRAME_TAG.containsMatchIn(inert), inert)
            assertTrue(scanHtml(inert).tags.none { it.name == "iframe" }, inert)
            assertTrue(inert.startsWith("<otus-inert-iframe"), inert)
            assertTrue(inert.endsWith("</otus-inert-iframe><p>After</p>"), inert)
        }
    }

    @Test
    fun plugInsAndFramesBecomeInert() {
        assertEquals(
            "<otus-inert-object data=x.swf><p>Fallback</p></otus-inert-object><otus-inert-embed src=y.swf>" +
                "<otus-inert-frameset><otus-inert-frame src=z.html></otus-inert-frameset>",
            inertFramesAndForms(
                "<object data=x.swf><p>Fallback</p></object><EMBED src=y.swf><frameset><frame src=z.html></frameset>",
            ),
        )
    }

    @Test
    fun aFramesFallbackMarkupStaysForTheOtherChecksToRead() {
        assertEquals(
            "<otus-inert-iframe><link rel=preconnect href=https://example.org/></otus-inert-iframe><p>After</p>",
            inertFramesAndForms("<iframe><link rel=preconnect href=https://example.org/></iframe><p>After</p>"),
        )
        assertEquals(
            "<otus-inert-iframe><otus-inert-link rel=preconnect href=https://example.org/></otus-inert-iframe>",
            inertLinks(inertFramesAndForms("<iframe><link rel=preconnect href=https://example.org/></iframe>")),
        )
    }

    @Test
    fun aFormCannotSubmit() {
        assertEquals(
            "<otus-inert-form action=\"https://example.org/x\"><input disabled name=\"q\">" +
                "<input disabled type=\"submit\"><button disabled>Go</button></otus-inert-form>",
            inertFramesAndForms(
                "<form action=\"https://example.org/x\"><input name=\"q\"><input type=\"submit\">" +
                    "<button>Go</button></form>",
            ),
        )
    }

    @Test
    fun controlsStayButTakeNoText() {
        assertEquals(
            "<SELECT disabled><option>A</select><textarea disabled><b>Kept as text</b></textarea>",
            inertFramesAndForms("<SELECT><option>A</select><textarea><b>Kept as text</b></textarea>"),
        )
    }

    @Test
    fun escapedTextStyleSheetsAndLongerNamesAreKept() {
        val html =
            "<style>iframe, form > input { color: red }</style>" +
                "<p>&lt;iframe&gt; and &lt;form&gt;</p><formula><frames><iframes>"
        assertEquals(html, inertFramesAndForms(html))
    }

    @Test
    fun theSameLettersInACommentOrAnAttributeChangeToo() {
        assertEquals(
            "<!-- <otus-inert-iframe srcdoc=x> --><p title=\"<otus-inert-embed src=x>\">Hi</p>",
            inertFramesAndForms("<!-- <iframe srcdoc=x> --><p title=\"<embed src=x>\">Hi</p>"),
        )
    }

    @Test
    fun aFrameOrFormIsInertHoweverTheMarkupBeforeItReads() {
        listOf(
            "<!-- --!><iframe srcdoc=\"$PRECONNECT\"></iframe> -->",
            "</ a=\"><iframe srcdoc=\"$PRECONNECT\"></iframe>\">",
            "<a x=y=\"><iframe srcdoc=\"$PRECONNECT\"></iframe>\">",
            "<a =\"><form action=https://example.org/x><input></form>\">",
            "<\u00e9 a=\"><iframe srcdoc=\"$PRECONNECT\"></iframe>\">",
        ).forEach { html ->
            assertFalse(LIVE_TAG.containsMatchIn(inertFramesAndForms(html)), html)
        }
    }

    private companion object {
        const val PRECONNECT = "&lt;link rel=preconnect href=https://example.org/&gt;"
        val FRAME_TAG = Regex("<iframe(?=[\\s/>])", RegexOption.IGNORE_CASE)
        val LIVE_TAG = Regex("<(?:iframe|form)(?=[\\s/>])", RegexOption.IGNORE_CASE)
    }
}
