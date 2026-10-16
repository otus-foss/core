package com.otus.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
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
class PullToRefreshScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun atRest() = compose.screenshot("pull_to_refresh_rest") { Inbox(refreshing = false) }

    @Test
    fun refreshing() =
        compose.screenshot(
            "pull_to_refresh_refreshing",
            { mainClock.advanceTimeBy(SPIN_MILLIS) },
        ) { Inbox(refreshing = true) }

    @Composable
    private fun Inbox(refreshing: Boolean) {
        compose.mainClock.autoAdvance = false
        PullToRefresh(refreshing, {}, Modifier.height(220.dp)) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                SettingRow("Lunch on Friday?", {}, description = "Mira Chen")
                SettingRow("Invoice 2026-031", {}, description = "Tomas Berg")
                SettingRow("Trip photos", {}, description = "Lena Park")
            }
        }
    }

    private companion object {
        const val SPIN_MILLIS = 300L
    }
}
