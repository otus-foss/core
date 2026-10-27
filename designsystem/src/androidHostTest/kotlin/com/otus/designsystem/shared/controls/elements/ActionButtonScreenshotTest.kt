package com.otus.designsystem.shared.controls.elements

import androidx.compose.material.Text
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.otus.designsystem.screenshot.screenshot
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
class ActionButtonScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun secondary() = compose.screenshot("action_button_secondary") { ActionButton("Cancel", {}) }

    @Test
    fun primary() = compose.screenshot("action_button_primary") { ActionButton("Send", {}, primary = true) }

    @Test
    fun disabled() = compose.screenshot("action_button_disabled") { ActionButton("Cancel", {}, enabled = false) }

    @Test
    fun primaryDisabled() =
        compose.screenshot("action_button_primary_disabled") {
            ActionButton("Send", {}, primary = true, enabled = false)
        }

    @Test
    fun customLabel() =
        compose.screenshot("action_button_custom_label") {
            ActionButton("Archive", {}, labelContent = { Text("Archive 12") })
        }

    @Test
    fun clickCallsOnClick() {
        var clicks = 0
        compose.setContent { ActionButton("Send", { clicks++ }) }
        compose.onNodeWithText("Send").performClick()
        assertEquals(1, clicks)
    }
}
