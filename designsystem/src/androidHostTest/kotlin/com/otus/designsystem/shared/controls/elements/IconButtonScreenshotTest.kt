package com.otus.designsystem.shared.controls.elements

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_close
import com.otus.designsystem.screenshot.screenshot
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
class IconButtonScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun outlined() = compose.screenshot("icon_button_outlined") { Close() }

    @Test
    fun plain() = compose.screenshot("icon_button_plain") { Close(look = IconButtonLook.Plain) }

    @Test
    fun disabled() = compose.screenshot("icon_button_disabled") { Close(enabled = false) }

    @Test
    fun clickCallsOnClick() {
        var clicks = 0
        compose.setContent { IconButton("Close", { clicks++ }) { Symbol(Res.drawable.symbol_close) } }
        compose.onNodeWithContentDescription("Close").performClick()
        assertEquals(1, clicks)
    }

    @Composable
    private fun Close(
        enabled: Boolean = true,
        look: IconButtonLook = IconButtonLook.Outlined,
    ) = Row { IconButton("Close", {}, enabled = enabled, look = look) { Symbol(Res.drawable.symbol_close) } }
}
