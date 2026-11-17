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
    fun aFrameDropsTheFallbackTextABrowserNeverShows() {
        assertEquals(
            "<otus-inert-iframe></otus-inert-iframe><p>After</p>",
            inertFramesAndForms("<iframe><link rel=preconnect href=https://example.org/></iframe><p>After</p>"),
        )
        assertEquals("<p>Hi</p><otus-inert-iframe>", inertFramesAndForms("<p>Hi</p><iframe><img src=x>"))
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
    fun tagsInTextCommentsAndStylesAreKept() {
        val html =
            "<!-- <iframe srcdoc=x><form> --><style>iframe, form > input { color: red }</style>" +
                "<p title=\"<embed src=x>\">&lt;iframe&gt; and &lt;form&gt;</p><formula><frames>"
        assertEquals(html, inertFramesAndForms(html))
    }

    private companion object {
        const val PRECONNECT = "&lt;link rel=preconnect href=https://example.org/&gt;"
        val FRAME_TAG = Regex("<iframe(?=[\\s/>])", RegexOption.IGNORE_CASE)
    }
}
