package com.otus.settings.presentation.components

import androidx.compose.runtime.Composable
import com.otus.designsystem.shared.settings.components.SettingPageRow
import com.otus.designsystem.shared.settings.components.SettingRow
import com.otus.designsystem.shared.settings.elements.ValueButton
import com.otus.settings.presentation.SettingsItem

/**
 * A search result under its page's heading: its name, its section or a top-level page's summary,
 * and its current value, or a chevron for a page. An unavailable setting or page says what it waits
 * on in place of its section, and still opens the page it is on, where that other setting is.
 */
@Composable
internal fun SettingsResultRow(item: SettingsItem) {
    if (item.isPage) {
        SettingPageRow(item.title, item::onOpen, description = item.unavailable ?: item.detail, icon = item.icon)
    } else {
        SettingRow(item.title, item::onOpen, description = item.unavailable ?: item.detail, icon = item.icon) {
            item.value?.let { ValueButton(it) }
        }
    }
}
