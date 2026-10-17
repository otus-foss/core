package com.otus.designsystem.components

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import java.io.File
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
        capture("toast-shown")
        compose.onNodeWithText("Undo").performClick()
        listOf(60L, 60L, 60L).forEachIndexed { step, millis ->
            compose.mainClock.advanceTimeBy(millis)
            capture("toast-leaving-${step + 1}")
        }
        compose.mainClock.advanceTimeBy(SETTLE)
        assertTrue(state.entries.isEmpty())
    }

    /** Saves the screen to the build's reports, where the toast's steps can be compared by eye. */
    private fun capture(name: String) {
        val out = File("build/reports/toasts").apply { mkdirs() }
        // A paused clock never finishes the redraw captureToImage waits for, so this draws the window itself.
        compose.runOnUiThread {
            val view = compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(out, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun textBottom() = compose.onNodeWithText("Sent", useUnmergedTree = true).getBoundsInRoot().bottom

    private companion object {
        val BAR = 80.dp
        const val SETTLE = 1_000L
    }
}
