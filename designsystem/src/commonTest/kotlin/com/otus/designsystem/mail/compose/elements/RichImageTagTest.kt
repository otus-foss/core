package com.otus.designsystem.mail.compose.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class RichImageTagTest {
    @Test
    fun anImageTagGivesItsSourceWidthAndDescription() {
        val image = "<img src=\"cid:a\" width=\"320\" alt=\"a &lt; b &quot;c&quot; &amp; d\" style=\"max-width:100%\">"
        val html = "<p>Before</p>$image<p>After</p>"
        val tag = imageTags(html).single()
        assertEquals("cid:a", tag.source)
        assertEquals(320, tag.width)
        assertEquals("a < b \"c\" & d", tag.description)
        assertEquals(image, html.substring(tag.start, tag.end))
    }

    @Test
    fun onlyWholeImageTagsWithASourceCount() {
        val html = "<img alt=\"none\"><img src=\"cid:a\" width=\"50%\"><!-- <img src=\"cid:b\"> --><img src=\"cid:c"
        val tags = imageTags(html)
        assertEquals(listOf("cid:a"), tags.map { it.source })
        assertEquals(null, tags.single().width)
    }

    @Test
    fun manyUnclosedImageTagsTakeLinearTime() {
        val start = TimeSource.Monotonic.markNow()
        assertTrue(imageTags("<img ".repeat(40_000)).isEmpty())
        val data = "<img src=\"data:image/png;base64," + "A".repeat(100_000) + "\">"
        assertEquals(3, imageTags(data.repeat(3)).size)
        assertTrue(start.elapsedNow() < 2.seconds, "Took ${start.elapsedNow()}")
    }
}
