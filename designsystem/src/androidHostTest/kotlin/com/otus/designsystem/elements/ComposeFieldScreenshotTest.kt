package com.otus.designsystem.elements

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
class ComposeFieldScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun empty() = compose.screenshot("compose_field_empty") { ComposeField("To", "", {}, WIDTH) }

    @Test
    fun placeholder() =
        compose.screenshot("compose_field_placeholder") {
            ComposeField("Subject", "", {}, WIDTH, placeholder = "Subject")
        }

    @Test
    fun filled() =
        compose.screenshot("compose_field_filled") {
            ComposeField("Subject", "Receipts for October", {}, WIDTH)
        }

    private companion object {
        val WIDTH = Modifier.width(320.dp)
    }
}
