package com.otus.settings.presentation.screens

import androidx.compose.runtime.Composable
import com.otus.designsystem.shared.pages.components.Subpage
import com.otus.settings.presentation.components.SettingSections
import com.otus.settings.presentation.components.SettingsSectionSpec

/**
 * Shared appearance settings, the [sections] that `appearanceSections` builds with the app's own
 * rows. Behavior, such as gestures or the sidebar, belongs on a page of the app's own.
 */
@Composable
public fun AppearanceSettingsScreen(
    sections: List<SettingsSectionSpec>,
    onBack: () -> Unit,
) {
    Subpage("Appearance", onBack) { SettingSections(sections) }
}
