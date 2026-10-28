package com.otus.designsystem.shared.selection.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.theme.colors.OtusColors
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WheelPickerScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun middle() =
        compose.screenshot("wheel_picker_middle") {
            WheelPicker("Snooze", OPTIONS, 3, {}, Modifier.size(200.dp, 240.dp).background(OtusColors.bgRegular))
        }

    @Test
    fun first() =
        compose.screenshot("wheel_picker_first") {
            WheelPicker("Snooze", OPTIONS, 0, {}, Modifier.size(200.dp, 240.dp).background(OtusColors.bgRegular))
        }

    @Test
    fun longOption() =
        compose.screenshot("wheel_picker_long_option") {
            WheelPicker("Snooze", LONG, 1, {}, Modifier.size(160.dp, 240.dp).background(OtusColors.bgRegular))
        }

    @Test
    fun settledWheelKeepsSelection() {
        val chosen = mutableListOf<Int>()
        compose.setContent { WheelPicker("Snooze", OPTIONS, 3, { chosen += it }, Modifier.size(200.dp, 240.dp)) }
        compose.waitForIdle()
        assertEquals(listOf(3), chosen.distinct())
    }

    private companion object {
        val OPTIONS = listOf("5 minutes", "15 minutes", "30 minutes", "1 hour", "3 hours", "Tomorrow", "Next week")
        val LONG = listOf("Later today", "Tomorrow morning at 8:00", "Next Monday")
    }
}
