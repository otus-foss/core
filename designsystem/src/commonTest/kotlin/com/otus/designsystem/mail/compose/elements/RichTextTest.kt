package com.otus.designsystem.mail.compose.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RichTextTest {
    @Test
    fun anImageInTheHtmlSplitsTheTextAroundIt() {
        val text = RichText("<p>Before</p><img src=\"cid:a\"><p>After</p>")
        assertEquals(listOf("cid:a"), text.images.sources)
        assertEquals("Before\nAfter", text.text)
        assertTrue(text.html.contains("<img src=\"cid:a\""))
    }

    @Test
    fun anInsertedImageGoesAtTheCursorAndTypingGoesOnAfterIt() {
        val text = RichText("<p>Hello</p>")
        text.images.insert("cid:a")
        assertEquals(listOf("cid:a"), text.images.sources)
        val blocks = text.blocks
        assertEquals(3, blocks.size)
        assertTrue(blocks[1] is RichBlock.Image)
        assertEquals<RichBlock?>(blocks.last(), text.active)
    }

    @Test
    fun removingAnImageJoinsTheTextAroundIt() {
        val text = RichText("<p>Before</p><img src=\"cid:a\"><p>After</p>")
        text.images.remove("cid:a")
        assertEquals(1, text.blocks.size)
        assertTrue(text.images.sources.isEmpty())
        assertTrue(text.text.startsWith("Before") && text.text.endsWith("After"))
    }
}
