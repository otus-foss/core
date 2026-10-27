package com.otus.designsystem.shared.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_archive
import com.otus.designsystem.resources.symbol_chevron_right
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.resources.symbol_inbox
import com.otus.designsystem.resources.symbol_send
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.people.components.DemoPeople
import com.otus.designsystem.shared.rows.components.RowDensity
import com.otus.designsystem.shared.rows.elements.RowButton
import com.otus.designsystem.shared.settings.elements.ValueButton
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
class SettingRowScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun regular() =
        compose.screenshot("setting_row_regular") {
            Column {
                SettingRow("Swipe actions", {}, description = "Archive to the right, delete to the left") {
                    Symbol(Res.drawable.symbol_chevron_right)
                }
                SettingRow("Accent", {}) { ValueButton("Blue", swatch = OtusColors.accentRegular) }
                SettingRow("work@example.com", null, description = "Signed in with JMAP") {
                    RowButton("Remove", {})
                }
            }
        }

    @Test
    fun withPerson() =
        compose.screenshot("setting_row_person") {
            SettingRow(
                "Receipts",
                {},
                description = "Mostly from Mira Chen",
                person = DemoPeople.mira,
                icon = Res.drawable.symbol_folder,
            )
        }

    @Test
    fun toggles() =
        compose.screenshot("setting_row_toggles") {
            Column {
                SettingToggleRow("Notifications", checked = true, {}, description = "New mail in Inbox only")
                SettingToggleRow("Load remote images", checked = false, {})
            }
        }

    @Test
    fun compact() =
        compose.screenshot("setting_row_compact") {
            Column {
                SettingRow("Inbox", {}, icon = Res.drawable.symbol_inbox, density = RowDensity.Compact)
                SettingRow("Sent", {}, icon = Res.drawable.symbol_send, density = RowDensity.Compact)
                SettingRow(
                    "Archive of every newsletter from the last ten years",
                    {},
                    icon = Res.drawable.symbol_archive,
                    density = RowDensity.Compact,
                ) { RowButton("Remove", {}) }
            }
        }
}
