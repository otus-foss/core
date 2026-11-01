package com.otus.designsystem.mail.read.elements

import com.otus.designsystem.theme.colors.OtusPalette
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class HtmlScanTest {
    private val key = RenderKey("k1")

    @Test
    fun tagsWithoutAnEndTakeLinearTime() {
        listOf("<a ".repeat(70_000), "<style>".repeat(30_000), "<img ".repeat(40_000), "<a color=".repeat(20_000))
            .forEach { html ->
                withinBudget {
                    hasOwnColors(html)
                    hasOwnLayout(html)
                    sandboxedHtml(html, loadRemote = false, OtusPalette.Otus, key, placeholders = true)
                }
            }
    }

    @Test
    fun aLongUnquotedImageSourceStaysAsItIs() {
        val html = "<p>Hi</p><img src=data:image/png;base64," + "A".repeat(100_000) + ">"
        withinBudget { assertEquals(html, withImagePlaceholders(html, key)) }
    }

    @Test
    fun deepNestingSealsInLinearTime() {
        val html = "<a>".repeat(70_000) + "</table>".repeat(20_000)
        withinBudget { assertTrue(sealedHtml(html).endsWith("</a>".repeat(70_000))) }
    }

    @Test
    fun aLongRunOfBracketsInTextTakesLinearTime() {
        val text = "[".repeat(40_000) + "[Otus](https://otus.example)"
        withinBudget { assertTrue(textHtml(text).endsWith("<a href=\"https://otus.example\">Otus</a>")) }
    }

    @Test
    fun attributesFollowTheBrowsersRules() {
        val html = "<IMG/SRC=https://a.example/x.png Alt='a > b' src=\"second\" hidden width = 20>"
        val tag = scanHtml(html).tags.single()
        assertEquals("img", tag.name)
        assertEquals(
            mapOf("src" to "https://a.example/x.png", "alt" to "a > b", "hidden" to "", "width" to "20"),
            tag.attributes,
        )
    }

    @Test
    fun onlyRealImageTagsBecomePlaceholders() {
        val kept =
            "<!-- <img src=https://t.example/c.png> --><p title=\"<img src=https://t.example/t.png>\">" +
                "<style>p{background:url(x)}</style><textarea><img src=https://t.example/a.png></textarea>"
        assertEquals(kept, withImagePlaceholders(kept, key))
        val blocked = withImagePlaceholders("<img/src=https://t.example/s.png>", key)
        assertFalse("t.example" in blocked)
        assertContains(blocked, "<a href=\"otus-images:show?k=k1\"")
    }

    @Test
    fun colorsAndLayoutComeFromTagsAndStyleSheetsOnly() {
        assertTrue(hasOwnColors("<span style=\"font-weight:bold;COLOR:red\">"))
        assertTrue(hasOwnColors("<style type=\"text/css\">\n.x { color : red }\n</style>"))
        assertTrue(hasOwnLayout("<div style='background-image:url(x)'>"))
        assertTrue(hasOwnLayout("<TABLE width=600>"))
        assertFalse(hasOwnColors("<p style=\"border-color:red\">a color = red</p>"))
        assertFalse(hasOwnColors("<!-- <font color=red> -->"))
        assertFalse(hasOwnLayout("<p>a &lt;table&gt; of contents</p>"))
    }

    private fun withinBudget(block: () -> Unit) {
        val start = TimeSource.Monotonic.markNow()
        block()
        val elapsed = start.elapsedNow()
        assertTrue(elapsed < BUDGET, "Took $elapsed")
    }

    private companion object {
        /** Far above the linear scan's few milliseconds, and far below the seconds to minutes backtracking took. */
        val BUDGET = 2.seconds
    }
}
