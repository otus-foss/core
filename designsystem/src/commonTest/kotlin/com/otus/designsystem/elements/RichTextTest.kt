package com.otus.designsystem.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RichTextTest {
    @Test
    fun anImageInTheHtmlSplitsTheTextAroundIt() {
        val text = RichText("<p>Before</p><img src=\"cid:a\"><p>After</p>")
        assertEquals(listOf("cid:a"), text.imageSources)
        assertEquals("Before\nAfter", text.text)
        assertTrue(text.html.contains("<img src=\"cid:a\""))
    }

    @Test
    fun anInsertedImageGoesAtTheCursorAndTypingGoesOnAfterIt() {
        val text = RichText("<p>Hello</p>")
        text.insertImage("cid:a")
        assertEquals(listOf("cid:a"), text.imageSources)
        val blocks = text.blocks
        assertEquals(3, blocks.size)
        assertTrue(blocks[1] is RichBlock.Image)
        assertEquals<RichBlock?>(blocks.last(), text.active)
    }

    @Test
    fun removingAnImageJoinsTheTextAroundIt() {
        val text = RichText("<p>Before</p><img src=\"cid:a\"><p>After</p>")
        text.removeImage("cid:a")
        assertEquals(1, text.blocks.size)
        assertTrue(text.imageSources.isEmpty())
        assertTrue(text.text.startsWith("Before") && text.text.endsWith("After"))
    }
}
