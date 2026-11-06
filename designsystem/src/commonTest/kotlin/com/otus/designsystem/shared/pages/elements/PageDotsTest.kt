package com.otus.designsystem.shared.pages.elements

import com.otus.designsystem.theme.tokens.layout.PageTokens
import kotlin.test.Test
import kotlin.test.assertEquals

class PageDotsTest {
    @Test
    fun fewPagesShowEveryDotAtFullSize() {
        val dots = visibleDots(count = 6, current = 4)
        assertEquals((0..5).toList(), dots.map { it.index })
        assertEquals(listOf(4), dots.filter { it.current }.map { it.index })
        assertEquals(setOf(PageTokens.dotSize), dots.map { it.size }.toSet())
    }

    @Test
    fun manyPagesKeepTheCurrentDotInAShrinkingWindow() {
        val middle = visibleDots(count = 20, current = 10)
        assertEquals((7..13).toList(), middle.map { it.index })
        assertEquals(
            listOf(PageTokens.edgeDot, PageTokens.nearEdgeDot, PageTokens.dotSize),
            middle.take(3).map { it.size },
        )
        assertEquals(listOf(PageTokens.nearEdgeDot, PageTokens.edgeDot), middle.takeLast(2).map { it.size })
        val first = visibleDots(count = 20, current = 0)
        assertEquals((0..6).toList(), first.map { it.index })
        assertEquals(PageTokens.dotSize, first.first().size)
        assertEquals(PageTokens.edgeDot, first.last().size)
        val last = visibleDots(count = 20, current = 19)
        assertEquals((13..19).toList(), last.map { it.index })
        assertEquals(PageTokens.dotSize, last.last().size)
    }
}
