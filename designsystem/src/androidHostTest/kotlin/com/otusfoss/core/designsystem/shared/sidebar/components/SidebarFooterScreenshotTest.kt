package com.otusfoss.core.designsystem.shared.sidebar.components

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
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
class SidebarFooterScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun upToDate() =
        compose.screenshot("sidebar_footer") {
            SidebarFooter("Nightly", "17.10.26", WIDTH)
        }

    @Test
    fun updateAvailable() =
        compose.screenshot("sidebar_footer_update") {
            SidebarFooter("Nightly", "17.10.26", WIDTH, onUpdate = {})
        }

    @Test
    fun updateDownloading() =
        compose.screenshot("sidebar_footer_downloading") {
            SidebarFooter("Nightly", "18.10.26", WIDTH, updateProgress = 0.6f)
        }

    @Test
    fun owlSmilesAfterTap() =
        compose.screenshot(
            "sidebar_footer_smile",
            {
                onNodeWithContentDescription("Otus owl").performClick()
                mainClock.advanceTimeBy(SMILE_MILLIS)
            },
        ) {
            compose.mainClock.autoAdvance = false
            SidebarFooter("Stable", "1.0", WIDTH)
        }

    private companion object {
        val WIDTH = Modifier.width(320.dp)

        /** Partway into the smile, while the owl squints. */
        const val SMILE_MILLIS = 400L
    }
}
