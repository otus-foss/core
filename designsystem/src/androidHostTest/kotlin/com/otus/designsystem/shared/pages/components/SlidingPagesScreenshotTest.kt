package com.otus.designsystem.shared.pages.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_edit
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.shared.actionbar.components.ActionBar
import com.otus.designsystem.shared.actionbar.components.ActionBarHost
import com.otus.designsystem.shared.actionbar.components.BarAction
import com.otus.designsystem.shared.controls.elements.UiAction
import com.otus.designsystem.shared.settings.components.SettingRow
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SlidingPagesScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val page = mutableIntStateOf(0)
    private val message = mutableStateOf<String?>(null)

    @Test
    fun pageMidSlide() =
        compose.screenshot("sliding_pages_mid_slide", { advanceHalfway { page.intValue = 1 } }) {
            SlidingPages(page.intValue, depth = { it }, Modifier.size(320.dp, 200.dp)) { Page(it) }
        }

    @Test
    fun overlayMidSlide() =
        compose.screenshot("sliding_overlay_mid_slide", { advanceHalfway { message.value = "Lunch on Friday?" } }) {
            SlidingOverlay(message.value, Modifier.size(320.dp, 200.dp), base = { Page(0) }) {
                PageScaffold(it, navigationLabel = "Back", onNavigation = {}) {}
            }
        }

    private fun advanceHalfway(change: () -> Unit) {
        compose.mainClock.autoAdvance = false
        change()
        // Sends the state change to the composition, which starts the slide on the next frame.
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(HALFWAY_MILLIS)
    }

    /** A page whose panel and bar stay still during a slide to another page; only its row moves. */
    @Composable
    private fun Page(depth: Int) =
        ActionBarHost(bar = {
            ActionBar(
                BarAction(UiAction(if (depth == 0) "Write" else "Save", Res.drawable.symbol_edit) {}),
            )
        }) {
            PageScaffold(if (depth == 0) "Inbox" else "Settings", navigationLabel = "Back", onNavigation = {}) {
                SettingRow(if (depth == 0) "Lunch on Friday?" else "Notifications", {})
            }
        }

    private companion object {
        const val HALFWAY_MILLIS = 128L
    }
}
