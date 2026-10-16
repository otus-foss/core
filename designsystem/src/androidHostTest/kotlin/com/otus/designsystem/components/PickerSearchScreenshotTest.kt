package com.otus.designsystem.components

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
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
class PickerSearchScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun empty() =
        compose.screenshot("picker_search_empty") { PickerSearch("", {}, "Search folders", Modifier.width(320.dp)) }

    @Test
    fun withText() =
        compose.screenshot("picker_search_text") {
            PickerSearch("rece", {}, "Search folders", Modifier.width(320.dp))
        }
}
