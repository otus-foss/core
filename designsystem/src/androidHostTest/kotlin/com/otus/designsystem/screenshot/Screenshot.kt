package com.otus.designsystem.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.OtusPalette
import com.otus.designsystem.theme.OtusTheme

private const val SHOT_TAG = "screenshot"

private fun shotPath(name: String) = "src/androidHostTest/screenshots/$name.png"

/**
 * Renders [content] in [palette] on the raised background and compares it with
 * `src/androidHostTest/screenshots/<name>.png`, or records that file with `-Proborazzi.record`.
 * [act] runs between composing and capturing, for example to drag a row or advance a paused clock.
 */
fun ComposeContentTestRule.screenshot(
    name: String,
    act: ComposeContentTestRule.() -> Unit = {},
    palette: OtusPalette = OtusPalette.Otus,
    content: @Composable () -> Unit,
) {
    setContent {
        OtusTheme(palette) {
            // OtusTheme's Surface stretches its child, so the tagged box sits inside a wrapper.
            Box(contentAlignment = Alignment.TopStart) {
                Box(Modifier.testTag(SHOT_TAG).background(OtusColors.bgDark).padding(8.dp)) { content() }
            }
        }
    }
    act()
    onNodeWithTag(SHOT_TAG).captureRoboImage(shotPath(name))
}

/**
 * Like [screenshot], but captures the whole screen, so content in its own window, such as a sheet,
 * a dialog, or a menu that [act] opens, is in the image too.
 */
@OptIn(ExperimentalRoborazziApi::class)
fun ComposeContentTestRule.screenScreenshot(
    name: String,
    act: ComposeContentTestRule.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    setContent { OtusTheme(content = content) }
    act()
    waitForIdle()
    captureScreenRoboImage(shotPath(name))
}
