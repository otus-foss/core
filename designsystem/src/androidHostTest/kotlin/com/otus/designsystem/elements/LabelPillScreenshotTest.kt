package com.otus.designsystem.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_folder
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
class LabelPillScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun plain() = compose.screenshot("label_pill_plain") { LabelPill("Receipts") }

    @Test
    fun icon() = compose.screenshot("label_pill_icon") { LabelPill("Receipts", icon = Res.drawable.symbol_folder) }

    @Test
    fun highlighted() =
        compose.screenshot("label_pill_highlighted") {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                LabelPill("Inbox", icon = Res.drawable.symbol_folder, highlighted = true)
                LabelPill("Receipts")
            }
        }

    @Test
    fun longText() =
        compose.screenshot("label_pill_long_text") {
            LabelPill(
                "Receipts from the Analytical Engine Society",
                Modifier.widthIn(max = 160.dp),
                icon = Res.drawable.symbol_folder,
            )
        }
}
