package com.otus.designsystem.elements

import androidx.compose.ui.test.junit4.v2.createComposeRule
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
class ChipScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun unselected() = compose.screenshot("chip_unselected") { Chip("Inbox", {}) }

    @Test
    fun selectedWithDetail() =
        compose.screenshot("chip_selected_detail") {
            Chip("Work", {}, selected = true, detail = "3")
        }

    @Test
    fun disabled() = compose.screenshot("chip_disabled") { Chip("Inbox", {}, enabled = false) }
}
