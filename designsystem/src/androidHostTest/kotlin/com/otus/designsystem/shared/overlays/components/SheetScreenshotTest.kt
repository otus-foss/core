package com.otus.designsystem.shared.overlays.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.screenshot.screenScreenshot
import com.otus.designsystem.shared.controls.elements.Chip
import com.otus.designsystem.shared.rows.components.SummaryCard
import com.otus.designsystem.shared.selection.components.PickerSearch
import com.otus.designsystem.shared.settings.components.SettingToggleRow
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SheetScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun sheetFrame() =
        compose.screenScreenshot("sheet_frame") {
            SheetFrame("Move to", onDismiss = {}) {
                SummaryCard("Invoice 2026-031", "Tomas Berg", Modifier.padding(16.dp))
            }
        }

    @Test
    fun selectionSheet() =
        compose.screenScreenshot("selection_sheet_search") {
            ListSheet(
                "From",
                {},
                done = SheetConfirm("Done", {}),
                onClear = {},
                search = { PickerSearch("", {}, "Search people") },
                scrolls = true,
            ) {
                SettingToggleRow("Mira Chen", checked = true, {})
                SettingToggleRow("Tomas Berg", checked = false, {})
                SettingToggleRow("Lena Park", checked = true, {})
            }
        }

    @Test
    fun selectionSheetApplyDisabled() =
        compose.screenScreenshot("selection_sheet_apply_disabled") {
            ListSheet("Folder", {}, done = SheetConfirm("Done", {}, enabled = false), onClear = {}, scrolls = true) {
                Chip("Receipts", {})
            }
        }
}
