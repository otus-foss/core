package com.otus.designsystem.shared.controls.elements

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_flag
import com.otus.designsystem.screenshot.screenshot
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class IconPillScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun unselected() =
        compose.screenshot("icon_pill_unselected") {
            IconPill("Flagged", Res.drawable.symbol_flag, selected = false, {})
        }

    @Test
    fun selected() =
        compose.screenshot("icon_pill_selected") { IconPill("Flagged", Res.drawable.symbol_flag, selected = true, {}) }

    @Test
    fun togglesSelection() {
        compose.setContent {
            var selected by remember { mutableStateOf(false) }
            IconPill("Flagged", Res.drawable.symbol_flag, selected, { selected = !selected })
        }
        val pill = compose.onNodeWithContentDescription("Flagged")
        pill.assertIsOff()
        pill.performClick()
        pill.assertIsOn()
    }
}
