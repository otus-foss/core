package com.otus.designsystem.shared.controls.elements

import androidx.compose.foundation.layout.Row
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_archive
import com.otus.designsystem.resources.symbol_chevron_left
import com.otus.designsystem.resources.symbol_inbox
import com.otus.designsystem.resources.symbol_send
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
class SymbolScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun sizes() =
        compose.screenshot("symbol_sizes") {
            Row {
                Symbol(Res.drawable.symbol_inbox, size = 16.dp)
                Symbol(Res.drawable.symbol_inbox)
                Symbol(Res.drawable.symbol_inbox, size = 32.dp)
            }
        }

    @Test
    fun contentColor() =
        compose.screenshot("symbol_content_color") {
            CompositionLocalProvider(LocalContentColor provides OtusColors.accentLight) {
                Row {
                    Symbol(Res.drawable.symbol_archive)
                    Symbol(Res.drawable.symbol_send)
                }
            }
        }

    /** Only the mirrored chevron flips; the second one stays as drawn. */
    @Test
    fun mirroredInRtl() =
        compose.screenshot("symbol_mirrored_rtl") {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row {
                    Symbol(Res.drawable.symbol_chevron_left, mirrorInRtl = true)
                    Symbol(Res.drawable.symbol_chevron_left)
                }
            }
        }
}
