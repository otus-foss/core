package com.otus.designsystem.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.otus.designsystem.elements.ToastAction
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.OtusTheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Toasts sit at the bottom of a page without a bar, and above the bar of a page with one. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h780dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BottomClearanceTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

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
                    // Padded like a page that keeps clear of the system bars itself.
                    ActionBarHost(bar.takeIf { withBar }, Modifier.fillMaxSize().padding(bottom = HOST_PADDING)) {}
                    ToastHost(state, Modifier.align(Alignment.BottomCenter))
                }
            }
        }
        compose.mainClock.advanceTimeBy(SETTLE)
        val aboveBar = textBottom()
        withBar = false
        compose.mainClock.advanceTimeBy(SETTLE)
        // Over the bar it keeps the bar's inset above it; without one, the same inset from the edge.
        assertEquals(BAR + ActionBarTokens.barInset + HOST_PADDING, textBottom() - aboveBar)
    }

    @Test
    fun undoSinksTheToastBehindTheBar() {
        val state = ToastState().apply { show(ToastMessage("Email deleted", ToastAction("Undo") {})) }
        compose.mainClock.autoAdvance = false
        compose.setContent {
            OtusTheme {
                CompositionLocalProvider(
                    LocalNavigationEventDispatcherOwner provides rememberNavigationEventDispatcherOwner(parent = null),
                    LocalBottomClearance provides remember { BottomClearance() },
                ) {
                    Box(Modifier.fillMaxSize().background(OtusColors.bgRegular)) {
                        val shape = RoundedCornerShape(ActionBarTokens.radius)
                        ActionBarHost(
                            { Box(Modifier.fillMaxWidth().height(BAR).background(OtusColors.bgLight, shape)) },
                            Modifier.fillMaxSize(),
                        ) {}
                        ToastHost(state, Modifier.align(Alignment.BottomCenter))
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(SETTLE)
        compose.captureWindow("toast-shown")
        compose.onNodeWithText("Undo").performClick()
        listOf(60L, 60L, 60L).forEachIndexed { step, millis ->
            compose.mainClock.advanceTimeBy(millis)
            compose.captureWindow("toast-leaving-${step + 1}")
        }
        compose.mainClock.advanceTimeBy(SETTLE)
        assertTrue(state.entries.isEmpty())
    }

    @Test
    fun draggingUncoversDismissAndStretchesThePill() {
        val state = ToastState().apply { show(ToastMessage("Email deleted", ToastAction("Undo") {})) }
        compose.mainClock.autoAdvance = false
        compose.setContent {
            OtusTheme {
                CompositionLocalProvider(
                    LocalNavigationEventDispatcherOwner provides rememberNavigationEventDispatcherOwner(parent = null),
                ) {
                    Box(Modifier.fillMaxSize().background(OtusColors.bgRegular)) {
                        ToastHost(state, Modifier.align(Alignment.Center))
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(SETTLE)
        val toast = compose.onNodeWithText("Email deleted")
        toast.performTouchInput { down(center) }
        listOf(40.dp, 40.dp, 40.dp, 40.dp, 80.dp, 80.dp).forEachIndexed { step, by ->
            toast.performTouchInput { moveBy(Offset(-by.toPx(), 0f)) }
            compose.mainClock.advanceTimeBy(FRAME)
            compose.captureWindow("toast-drag-${step + 1}")
        }
        toast.performTouchInput { up() }
    }

    private fun textBottom() = compose.onNodeWithText("Sent", useUnmergedTree = true).getBoundsInRoot().bottom

    private companion object {
        val BAR = 80.dp
        val HOST_PADDING = 24.dp
        const val SETTLE = 1_000L
        const val FRAME = 100L
    }
}
