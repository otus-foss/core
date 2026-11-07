package com.otus.designsystem.shared.controls.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_mark_email_unread
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.shared.folders.elements.FolderGlyph
import com.otus.designsystem.theme.colors.FolderColor
import com.otus.designsystem.theme.colors.ThemeBase
import com.otus.designsystem.theme.colors.ThemeChoice
import com.otus.designsystem.theme.tokens.controls.ControlTokens
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RaisedChipScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun states() = compose.screenshot("filter_chip_states") { Chips() }

    @Test
    fun light() = compose.screenshot("filter_chip_light", palette = ThemeChoice(ThemeBase.Light).palette) { Chips() }

    @Test
    fun badge() =
        compose.screenshot("badged_symbol") {
            IconButton("Open filters", {}, badgeCount = 3) { Symbol(Res.drawable.symbol_mark_email_unread) }
        }
}

@Composable
private fun Chips() {
    Column {
        Chip("Unread", {}, selected = true, style = ChipStyle(look = ChipLook.Raised)) {
            Symbol(Res.drawable.symbol_mark_email_unread, size = ControlTokens.chipIconSize)
        }
        Chip("Last 7 days", {}, style = ChipStyle(ChipRole.Radio, ChipLook.Raised))
        Chip(ChipLabel("Invoices", "Finance"), {
        }, selected = true, style = ChipStyle(look = ChipLook.Raised)) {
            FolderGlyph(
                FolderColor.Yellow,
                size = ControlTokens.chipIconSize,
            )
        }
        Chip("All folders…", {}, style = ChipStyle(ChipRole.Button, ChipLook.Raised)) {
            FolderGlyph(FolderColor.Default, size = ControlTokens.chipIconSize)
        }
    }
}
