package com.otus.designsystem.shared.pages.elements

import com.otus.designsystem.theme.ReadTokens
import kotlin.test.Test
import kotlin.test.assertEquals

class PageDotsTest {
    @Test
    fun fewPagesShowEveryDotAtFullSize() {
        val dots = visibleDots(count = 6, current = 4)
        assertEquals((0..5).toList(), dots.map { it.index })
        assertEquals(listOf(4), dots.filter { it.current }.map { it.index })
        assertEquals(setOf(ReadTokens.dotSize), dots.map { it.size }.toSet())
    }

    @Test
    fun manyPagesKeepTheCurrentDotInAShrinkingWindow() {
        val middle = visibleDots(count = 20, current = 10)
        assertEquals((7..13).toList(), middle.map { it.index })
        assertEquals(
            listOf(ReadTokens.edgeDot, ReadTokens.nearEdgeDot, ReadTokens.dotSize),
            middle.take(3).map { it.size },
        )
        assertEquals(listOf(ReadTokens.nearEdgeDot, ReadTokens.edgeDot), middle.takeLast(2).map { it.size })
        val first = visibleDots(count = 20, current = 0)
        assertEquals((0..6).toList(), first.map { it.index })
        assertEquals(ReadTokens.dotSize, first.first().size)
        assertEquals(ReadTokens.edgeDot, first.last().size)
        val last = visibleDots(count = 20, current = 19)
        assertEquals((13..19).toList(), last.map { it.index })
        assertEquals(ReadTokens.dotSize, last.last().size)
    }
}
