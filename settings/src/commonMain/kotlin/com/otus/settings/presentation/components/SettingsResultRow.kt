package com.otus.settings.presentation.components

import androidx.compose.runtime.Composable
import com.otus.designsystem.shared.settings.components.SettingPageRow
import com.otus.designsystem.shared.settings.components.SettingRow
import com.otus.designsystem.shared.settings.elements.ValueButton
import com.otus.settings.presentation.SettingsItem

/**
 * A search result under its page's heading: its name, its section or a top-level page's summary,
 * and its current value, or a chevron for a page.
 */
@Composable
internal fun SettingsResultRow(item: SettingsItem) {
    val detail = item.section ?: item.summary
    if (item.isPage) {
        SettingPageRow(item.title, item.onOpen, description = detail, icon = item.icon)
    } else {
        SettingRow(item.title, item.onOpen, description = detail, icon = item.icon) {
            item.value?.let { ValueButton(it) }
        }
    }
}
