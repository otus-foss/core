package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_archive
import com.otus.designsystem.resources.symbol_delete
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.theme.EntryTokens
import com.otus.designsystem.theme.InboxTokens
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
class SwipeActionsScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun atRest() = compose.screenshot("swipe_actions_rest") { Rows() }

    @Test
    fun justStarted() = compose.screenshot("swipe_actions_just_started", { drag(48f) }) { Rows() }

    @Test
    fun partlyRevealed() = compose.screenshot("swipe_actions_partly_revealed", { drag(160f) }) { Rows() }

    @Test
    fun pastThreshold() = compose.screenshot("swipe_actions_past_threshold", { drag(-400f) }) { Rows() }

    /** Holds the finger down after dragging the middle row by [distance] pixels, so the row never settles. */
    private fun ComposeContentTestRule.drag(distance: Float) {
        mainClock.autoAdvance = false
        onNodeWithTag(SWIPED).performTouchInput {
            down(center)
            repeat(STEPS) { moveBy(Offset(distance / STEPS, 0f)) }
        }
        mainClock.advanceTimeByFrame()
    }

    @Composable
    private fun Rows() =
        // The list's own surface and spacing, which show around the cards.
        Column(
            Modifier.background(OtusColors.bgRegular).padding(InboxTokens.pageInset),
            verticalArrangement = Arrangement.spacedBy(EntryTokens.gap),
        ) {
            Row("Mira Chen", "Lunch on Friday?")
            Row("Tomas Berg", "Invoice 2026-031", Modifier.testTag(SWIPED))
            Row("Lena Park", "Trip photos")
        }

    @Composable
    private fun Row(
        sender: String,
        subject: String,
        modifier: Modifier = Modifier,
    ) = SwipeActions(
        SwipeAction("Archive", Res.drawable.symbol_archive) {},
        SwipeAction("Delete", Res.drawable.symbol_delete, dismisses = true) {},
        modifier,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(OtusColors.bgDark, RoundedCornerShape(EntryTokens.radius))
                .padding(16.dp, 12.dp),
        ) {
            Text(sender)
            Text(subject, color = OtusColors.text2)
        }
    }

    private companion object {
        const val SWIPED = "swiped"
        const val STEPS = 10
    }
}
