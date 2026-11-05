package com.otus.designsystem.shared.contacts.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

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

    private companion object {
        val LETTERS = listOf("B", "M", "S").map { IndexLetter(it, count = 1) }
    }
}
