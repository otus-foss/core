package com.otus.designsystem.shared.controls.elements

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.theme.OtusColors
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ControlPressFeedbackScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun idle() = compose.screenshot("control_press_feedback_idle") { Shapes(null) }

    @Test
    fun pressed() = compose.screenshot("control_press_feedback_pressed") { Shapes(PressInteraction.Press(Offset.Zero)) }

    @Test
    fun focused() = compose.screenshot("control_press_feedback_focused") { Shapes(FocusInteraction.Focus()) }

    @Composable
    private fun Shapes(interaction: Interaction?) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Control(CircleShape, interaction)
            Control(RoundedCornerShape(8.dp), interaction)
        }
    }

    @Composable
    private fun Control(
        shape: Shape,
        interaction: Interaction?,
    ) {
        val interactions = remember { MutableInteractionSource() }
        LaunchedEffect(interaction) {
            // The feedback starts collecting in its own effect, so wait a frame before emitting.
            withFrameNanos {}
            interaction?.let { interactions.emit(it) }
        }
        Box(
            Modifier
                .size(80.dp, 40.dp)
                .border(1.dp, OtusColors.strokeBgDark, shape)
                .controlPressFeedback(interactions, shape),
        )
    }
}
