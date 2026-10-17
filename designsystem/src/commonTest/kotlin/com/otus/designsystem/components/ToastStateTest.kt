package com.otus.designsystem.components

import com.otus.designsystem.elements.ToastAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToastStateTest {
    private val undo = ToastAction("Undo") {}

    private fun ToastState.texts() = entries.map { it.message.text }

    private fun ToastState.advance(millis: Long) = tick(millis, ::baseTimeout)

    @Test
    fun newToastsStackNewestLast() {
        val state = ToastState()
        state.show(ToastMessage("Email deleted", undo))
        state.show(ToastMessage("Draft saved"))
        assertEquals(listOf("Email deleted", "Draft saved"), state.texts())
    }

    @Test
    fun sameGroupReplacesTheToastMovesItToFrontAndRestartsItsTimer() {
        val state = ToastState()
        state.show(ToastMessage("Email deleted", undo, group = "delete"))
        state.show(ToastMessage("Draft saved"))
        state.advance(2_000)
        state.show(ToastMessage("2 emails deleted", undo, group = "delete"))
        assertEquals(listOf("Draft saved", "2 emails deleted"), state.texts())
        assertEquals(0L, state.entries.last().elapsedMillis)
        assertEquals(0L, state.entries.last().id)
    }

    @Test
    fun aGroupIsShowingUntilItsToastLeaves() {
        val state = ToastState()
        state.show(ToastMessage("Email deleted", undo, group = "delete"))
        assertTrue(state.isShowing("delete"))
        assertFalse(state.isShowing("archive"))
        state.advance(6_000)
        assertFalse(state.isShowing("delete"))
        assertTrue(state.messages.isEmpty())
    }

    @Test
    fun ungroupedToastsNeverMerge() {
        val state = ToastState()
        state.show(ToastMessage("Link copied"))
        state.show(ToastMessage("Link copied"))
        assertEquals(2, state.entries.size)
    }

    @Test
    fun toastsLeaveWhenTheirOwnTimeIsUp() {
        val state = ToastState()
        state.show(ToastMessage("Draft saved"))
        state.show(ToastMessage("Email deleted", undo))
        state.advance(4_000)
        assertEquals(listOf(true, false), state.entries.map { it.leaving })
        state.advance(2_000)
        assertTrue(state.entries.all { it.leaving })
    }

    @Test
    fun aStickyToastWaitsAndOnlyAToastLeftWithoutItsActionReportsClosing() {
        val state = ToastState()
        var closed = 0
        state.show(ToastMessage("Couldn't send", undo, sticky = true))
        state.advance(60_000)
        assertFalse(state.entries.single().leaving)
        state.remove(state.entries.single(), acted = true)
        state.show(ToastMessage("Draft discarded", undo, onClose = { closed++ }))
        state.remove(state.entries.single(), acted = true)
        assertEquals(0, closed, "Undo ran, so nothing is left to finish")
        state.show(ToastMessage("Draft discarded", undo, onClose = { closed++ }))
        state.advance(6_000)
        state.remove(state.entries.single())
        assertEquals(1, closed)
    }

    @Test
    fun aToastCanStayForItsOwnDuration() {
        val state = ToastState()
        state.show(ToastMessage("Sending…", undo, durationMillis = 30_000))
        state.advance(29_000)
        assertFalse(state.entries.single().leaving)
        state.advance(1_000)
        assertTrue(state.entries.single().leaving)
    }

    @Test
    fun fanningOutPausesEveryTimer() {
        val state = ToastState()
        state.show(ToastMessage("Draft saved"))
        state.expanded = true
        state.advance(10_000)
        assertEquals(0L, state.entries.single().elapsedMillis)
    }

    @Test
    fun aHeldToastKeepsItsTime() {
        val state = ToastState()
        state.show(ToastMessage("Email deleted", undo))
        state.entries.single().held = true
        state.advance(10_000)
        assertFalse(state.entries.single().leaving)
        assertEquals(1f, state.entries.single().remaining)
    }

    @Test
    fun theRingFollowsTheTimeout() {
        val state = ToastState()
        state.show(ToastMessage("Email deleted", undo))
        state.tick(1_000) { 4_000 }
        assertEquals(0.75f, state.entries.single().remaining)
    }

    @Test
    fun removingDownToOneToastStacksTheGroup() {
        val state = ToastState()
        state.show(ToastMessage("Draft saved"))
        state.show(ToastMessage("Link copied"))
        state.expanded = true
        state.remove(state.entries.first())
        assertFalse(state.expanded)
    }
}
