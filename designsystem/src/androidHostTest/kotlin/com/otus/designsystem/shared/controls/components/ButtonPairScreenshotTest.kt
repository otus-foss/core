package com.otus.designsystem.shared.controls.components

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
class ButtonPairScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun enabled() = compose.screenshot("button_pair") { ButtonPair(PairButton("Cancel", {}), PairButton("Delete", {})) }

    @Test
    fun primaryDisabled() =
        compose.screenshot("button_pair_primary_disabled") {
            ButtonPair(PairButton("Cancel", {}), PairButton("Add", {}, enabled = false))
        }

    @Test
    fun secondaryDisabledAndBusy() =
        compose.screenshot("button_pair_busy") {
            ButtonPair(
                PairButton("Reset", {}, enabled = false),
                PairButton("Show conversations", {}),
                primaryBusy = true,
            )
        }

    @Test
    fun longPrimaryLabelWidens() =
        compose.screenshot("button_pair_long_primary") {
            ButtonPair(PairButton("Cancel", {}), PairButton("Trust mail.example.com", {}))
        }

    @Test
    fun eachHalfRunsItsOwnAction() {
        val calls = mutableListOf<String>()
        compose.setContent {
            ButtonPair(PairButton("Cancel", { calls += "cancel" }), PairButton("Delete", { calls += "delete" }))
        }
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Delete").performClick()
        assertEquals(listOf("cancel", "delete"), calls)
    }

    @Test
    fun aDisabledHalfIgnoresTaps() {
        var calls = 0
        compose.setContent {
            ButtonPair(PairButton("Reset", { calls++ }, enabled = false), PairButton("Apply", { calls++ }))
        }
        compose.onNodeWithText("Reset").performClick()
        assertEquals(0, calls)
    }
}
