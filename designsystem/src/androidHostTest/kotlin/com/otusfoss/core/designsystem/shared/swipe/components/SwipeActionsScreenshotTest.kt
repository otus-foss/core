package com.otusfoss.core.designsystem.shared.swipe.components

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
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_archive
import com.otusfoss.core.designsystem.resources.symbol_delete
import com.otusfoss.core.designsystem.screenshot.screenshot
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.rows.EntryTokens
import com.otusfoss.core.designsystem.theme.tokens.rows.InboxTokens
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
        // The list's own surface and spacing, which show around the cards; rows inset themselves.
        Column(
            Modifier.background(OtusColors.bgRegular).padding(vertical = InboxTokens.pageInset),
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
        SwipeAction(UiAction("Archive", Res.drawable.symbol_archive) {}),
        SwipeAction(UiAction("Delete", Res.drawable.symbol_delete) {}, dismisses = true),
        modifier,
        edgeInset = InboxTokens.pageInset,
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
