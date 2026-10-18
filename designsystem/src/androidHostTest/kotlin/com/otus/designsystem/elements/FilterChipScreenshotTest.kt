package com.otus.designsystem.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_mark_email_unread
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.theme.FilterTokens
import com.otus.designsystem.theme.FolderColor
import com.otus.designsystem.theme.ThemeBase
import com.otus.designsystem.theme.ThemeChoice
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FilterChipScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun states() = compose.screenshot("filter_chip_states") { Chips() }

    @Test
    fun light() = compose.screenshot("filter_chip_light", palette = ThemeChoice(ThemeBase.Light).palette) { Chips() }

    @Test
    fun badge() =
        compose.screenshot("badged_symbol") {
            IconButton("Open filters", {}) { BadgedSymbol(Res.drawable.symbol_mark_email_unread, 3) }
        }
}

@Composable
private fun Chips() {
    Column {
        FilterChip("Unread", selected = true, onClick = {}) {
            Symbol(Res.drawable.symbol_mark_email_unread, size = FilterTokens.chipIconSize)
        }
        FilterChip("Last 7 days", selected = false, onClick = {}, role = FilterChipRole.Radio)
        FilterChip(ChipLabel("Invoices", "Finance"), selected = true, onClick = {
        }) { FolderGlyph(FolderColor.Yellow, size = FilterTokens.chipIconSize) }
        FilterChip("All folders…", selected = false, onClick = {}, role = FilterChipRole.Button) {
            FolderGlyph(FolderColor.Default, size = FilterTokens.chipIconSize)
        }
    }
}
