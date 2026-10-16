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
class ComposeBodyScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun empty() = compose.screenshot("compose_body_empty") { ComposeBody("", {}, "Message", Modifier.width(320.dp)) }

    @Test
    fun text() =
        compose.screenshot("compose_body_text") {
            ComposeBody(
                "Hi Ada,\n\nThe receipts from last week are attached. " +
                    "Let me know if any of the totals look off before Friday.",
                {},
                "Message",
                Modifier.width(320.dp),
            )
        }
}
