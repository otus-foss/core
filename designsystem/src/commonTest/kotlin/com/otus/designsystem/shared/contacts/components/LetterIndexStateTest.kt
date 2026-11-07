package com.otus.designsystem.shared.contacts.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LetterIndexStateTest {
    @Test
    fun aPickedLetterKeepsTheHighlightWhileTheIndexCloses() {
        val state = LetterIndexState()
        state.open(LETTERS, onPick = {}, anchor = "M")
        assertEquals("M", state.highlighted)

        state.pick("S")

        assertFalse(state.open)
        assertEquals("S", state.highlighted)
    }

    @Test
    fun reopeningHighlightsThePressedLetterAgain() {
        val state = LetterIndexState()
        state.open(LETTERS, onPick = {}, anchor = "M")
        state.pick("S")

        state.open(LETTERS, onPick = {}, anchor = "B")

        assertEquals("B", state.highlighted)
    }

    @Test
    fun liftingOnTheSwitchButtonSwitchesSidesAndLeavesTheIndexOpen() {
        val state = LetterIndexState()
        var switched = false
        state.onSwitchSide = { switched = true }
        state.switchBounds = Rect(0f, 0f, 100f, 100f)
        state.open(LETTERS, onPick = {}, anchor = "M")

        state.drag(Offset(50f, 50f))

        assertTrue(state.onSwitch)
        assertNull(state.active)
        state.switchSide()
        assertTrue(switched)
        assertFalse(state.onSwitch)
        assertTrue(state.open)
    }

    @Test
    fun theOtherSideOfEachSideIsTheOpposite() {
        assertEquals(LetterIndexSide.End, LetterIndexSide.Start.other)
        assertEquals(LetterIndexSide.Start, LetterIndexSide.End.other)
    }

    private companion object {
        val LETTERS = listOf("B", "M", "S").map { IndexLetter(it, count = 1) }
    }
}
