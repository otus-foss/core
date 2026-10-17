package com.otus.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.otus.designsystem.theme.ActionBarTokens
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

/** Toasts sit at the bottom of a page without a bar, and above the bar of a page with one. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BottomClearanceTest {
    @get:Rule
    val compose = createComposeRule()

    private var withBar by mutableStateOf(true)

    @Test
    fun aToastRisesAboveABarAndDropsToTheBottomWithoutOne() {
        val state = ToastState().apply { show(ToastMessage("Sent")) }
        // A clock that runs on its own would let the toast expire between the checks.
        compose.mainClock.autoAdvance = false
        compose.setContent {
            CompositionLocalProvider(
                LocalNavigationEventDispatcherOwner provides rememberNavigationEventDispatcherOwner(parent = null),
                LocalBottomClearance provides remember { BottomClearance() },
            ) {
                Box(Modifier.fillMaxSize()) {
                    val bar = @Composable { Box(Modifier.width(200.dp).height(BAR)) }
                    ActionBarHost(bar.takeIf { withBar }, Modifier.fillMaxSize()) {}
                    ToastHost(state, Modifier.align(Alignment.BottomCenter))
                }
            }
        }
        compose.mainClock.advanceTimeBy(SETTLE)
        val aboveBar = textBottom()
        withBar = false
        compose.mainClock.advanceTimeBy(SETTLE)
        // Over the bar it keeps the bar's inset above it; without one, the same inset from the edge.
        assertEquals(BAR + ActionBarTokens.barInset, textBottom() - aboveBar)
    }

    private fun textBottom() = compose.onNodeWithText("Sent", useUnmergedTree = true).getBoundsInRoot().bottom

    private companion object {
        val BAR = 80.dp
        const val SETTLE = 1_000L
    }
}
