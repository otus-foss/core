package com.otus.designsystem.shared.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.shared.feedback.components.InfoText
import com.otus.designsystem.shared.people.components.DemoPeople
import com.otus.designsystem.shared.settings.elements.ValueButton
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsSectionScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun withAvatar() =
        compose.screenshot("settings_section_avatar") {
            Column {
                SettingsSection("Work", avatar = DemoPeople.mira.avatar) {
                    SettingRow("Drafts", {}) { ValueButton("Drafts") }
                    SettingRow("Sent", {}) { ValueButton("Sent Items") }
                }
                SettingsSection("Personal") {
                    SettingToggleRow("Mark read on open", checked = true, {})
                }
            }
        }

    @Test
    fun withInfo() =
        compose.screenshot("settings_section_info") {
            Column {
                SettingsSection("New mail", info = InfoText("New mail", "Who sees what when mail arrives.")) {
                    SettingToggleRow("Notify", checked = true, {})
                }
                SettingsSection("Personal") {
                    SettingToggleRow("Mark read on open", checked = true, {})
                }
            }
        }
}
