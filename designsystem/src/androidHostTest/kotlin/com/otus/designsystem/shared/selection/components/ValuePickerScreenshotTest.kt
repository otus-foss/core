package com.otus.designsystem.shared.selection.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.shared.people.components.DemoPeople
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ValuePickerScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun enabled() =
        compose.screenshot("value_picker_enabled") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ValuePicker("Receipts", {}, keyword = "Move to", icon = Res.drawable.symbol_folder)
                ValuePicker("Mira Chen", {}, keyword = "From", avatar = DemoPeople.mira.avatar)
                ValuePicker("Archive", {}, Modifier.fillMaxWidth(), icon = Res.drawable.symbol_folder)
            }
        }

    @Test
    fun disabled() =
        compose.screenshot("value_picker_disabled") {
            ValuePicker("Receipts", {}, keyword = "Move to", icon = Res.drawable.symbol_folder, enabled = false)
        }
}
