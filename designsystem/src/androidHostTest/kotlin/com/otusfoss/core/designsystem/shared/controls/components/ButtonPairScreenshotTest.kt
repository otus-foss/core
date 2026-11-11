package com.otusfoss.core.designsystem.shared.controls.components

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.otusfoss.core.designsystem.screenshot.screenshot
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
class ButtonPairScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun enabled() =
        compose.screenshot("button_pair") {
            ButtonPair(UiAction("Cancel", onClick = {}), UiAction("Delete", onClick = {}))
        }

    @Test
    fun primaryDisabled() =
        compose.screenshot("button_pair_primary_disabled") {
            ButtonPair(UiAction("Cancel", onClick = {}), UiAction("Add", enabled = false, onClick = {}))
        }

    @Test
    fun longPrimaryLabelWidens() =
        compose.screenshot("button_pair_long_primary") {
            ButtonPair(UiAction("Cancel", onClick = {}), UiAction("Trust mail.example.com", onClick = {}))
        }

    @Test
    fun eachHalfRunsItsOwnAction() {
        val calls = mutableListOf<String>()
        compose.setContent {
            ButtonPair(
                UiAction("Cancel", onClick = { calls += "cancel" }),
                UiAction("Delete", onClick = {
                    calls +=
                        "delete"
                }),
            )
        }
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Delete").performClick()
        assertEquals(listOf("cancel", "delete"), calls)
    }

    @Test
    fun aDisabledHalfIgnoresTaps() {
        var calls = 0
        compose.setContent {
            ButtonPair(
                UiAction("Reset", enabled = false, onClick = { calls++ }),
                UiAction("Apply", onClick = { calls++ }),
            )
        }
        compose.onNodeWithText("Reset").performClick()
        assertEquals(0, calls)
    }
}
