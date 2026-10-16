package com.otus.designsystem.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.otus.designsystem.elements.ToastAction
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ToastHostTest {
    @get:Rule
    val compose = createComposeRule()

    private val state = ToastState()

    // Timers run every frame, so a clock that advances on its own would let toasts expire before a check.
    // The test activity provides no back dispatcher, which ToastHost needs to stack the group on back.
    private fun showHost() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            CompositionLocalProvider(
                LocalNavigationEventDispatcherOwner provides rememberNavigationEventDispatcherOwner(parent = null),
            ) {
                ToastHost(state)
            }
        }
        settle()
    }

    /** Lets running animations finish, well within any toast's time. */
    private fun settle() = compose.mainClock.advanceTimeBy(1_000)

    @Test
    fun tappingTheActionRunsIt() {
        var undone = 0
        state.show(ToastMessage("Email deleted", ToastAction("Undo") { undone++ }))
        showHost()
        compose.onNodeWithText("Undo").performClick()
        assertEquals(1, undone)
    }

    @Test
    fun swipingPastAThirdDismissesInEitherDirection() {
        state.show(ToastMessage("Draft saved"))
        state.show(ToastMessage("Link copied"))
        showHost()
        compose.onNodeWithText("Link copied").performTouchInput { swipeLeft(right, left) }
        settle()
        assertEquals(listOf("Draft saved"), state.entries.map { it.message.text })
        compose.onNodeWithText("Draft saved").performTouchInput { swipeRight(left, right) }
        settle()
        assertTrue(state.entries.isEmpty())
    }

    @Test
    fun aShortSwipeSpringsBack() {
        var undone = false
        state.show(ToastMessage("Email deleted", ToastAction("Undo") { undone = true }))
        showHost()
        compose.onNodeWithText("Email deleted").performTouchInput { swipeRight(centerX, centerX + width / 6f) }
        settle()
        assertEquals(1, state.entries.size)
        assertFalse(state.entries.single().held)
        assertFalse(undone)
    }

    @Test
    fun tappingTheStackFansItOutAndTappingAgainStacksIt() {
        state.show(ToastMessage("Draft saved"))
        state.show(ToastMessage("Link copied"))
        showHost()
        compose.onNodeWithText("Link copied").performClick()
        settle()
        assertTrue(state.expanded)
        compose.onNodeWithText("Draft saved").performClick()
        assertFalse(state.expanded)
    }

    @Test
    fun screenReadersCanDismissWithoutTheSwipe() {
        state.show(ToastMessage("Draft saved"))
        showHost()
        val actions = compose.onNodeWithText("Draft saved").fetchSemanticsNode().config[SemanticsActions.CustomActions]
        compose.runOnIdle { actions.single { it.label == "Dismiss" }.action() }
        assertTrue(state.entries.isEmpty())
    }

    @Test
    fun aPlainToastFadesOutWhenItsTimeIsUp() {
        state.show(ToastMessage("Draft saved"))
        showHost()
        compose.mainClock.advanceTimeBy(4_000)
        assertTrue(state.entries.isEmpty())
    }
}
