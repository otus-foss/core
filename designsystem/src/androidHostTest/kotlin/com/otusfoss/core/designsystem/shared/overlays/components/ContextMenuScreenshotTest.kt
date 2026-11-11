package com.otusfoss.core.designsystem.shared.overlays.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.otusfoss.core.designsystem.screenshot.screenScreenshot
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ContextMenuScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val actions =
        listOf("Rename", "Duplicate", "Delete theme").map { UiAction(it, onClick = {}) }

    @Test
    fun atPoint() = compose.screenScreenshot("context_menu") { Page(Offset(120f, 300f)) }

    // The window is 720 by 1280 pixels; a press near its corner flips the menu up and to the left.
    @Test
    fun flipsNearCorner() = compose.screenScreenshot("context_menu_flipped") { Page(Offset(660f, 1220f)) }

    @Test
    fun choosingDismissesThenRuns() {
        val calls = mutableListOf<String>()
        compose.setContent {
            ContextMenu(listOf(UiAction("Rename") { calls += "rename" }), Offset.Zero) { calls += "dismiss" }
        }
        compose.onNodeWithText("Rename").performClick()
        assertEquals(listOf("dismiss", "rename"), calls)
    }

    @Composable
    private fun Page(at: Offset) {
        Box(Modifier.fillMaxSize()) { ContextMenu(actions, at) {} }
    }
}
