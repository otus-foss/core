package com.otusfoss.core.designsystem.shared.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.screenshot.screenshot
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PreviewCardScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun withHeader() =
        compose.screenshot("preview_card_header") {
            PreviewCard(
                SketchMarker.Row,
                title = "Inbox",
                description = "Archive, Move, Delete",
                onClick = {},
            ) { Text("Action bar preview") }
        }

    @Test
    fun markers() =
        compose.screenshot("preview_card_markers") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PreviewCard(SketchMarker.None) { Text("Open message") }
                PreviewCard(SketchMarker.Selected) { Text("Selection") }
            }
        }
}
