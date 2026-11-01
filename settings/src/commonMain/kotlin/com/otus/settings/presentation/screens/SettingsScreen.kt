package com.otus.settings.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.otus.designsystem.shared.pages.components.PageNote
import com.otus.designsystem.shared.pages.components.PageScaffold
import com.otus.designsystem.shared.panels.components.PageSearch
import com.otus.designsystem.shared.settings.components.SettingPageRow
import com.otus.settings.presentation.SettingsItem
import com.otus.settings.presentation.components.SettingsResultRow
import com.otus.settings.presentation.search

/**
 * The settings home: one row per top-level page of [index]. A search turns the list into every
 * page and setting that matches, best first.
 */
@Composable
public fun SettingsScreen(
    index: List<SettingsItem>,
    onOpenNavigation: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    PageScaffold(
        "Settings",
        navigationLabel = "Open navigation",
        onNavigation = onOpenNavigation,
        search = PageSearch(query, { query = it }, "Search settings"),
    ) {
        if (query.isBlank()) {
            Column {
                index
                    .filter { it.isPage && it.path.isEmpty() }
                    .forEach { SettingPageRow(it.title, it.onOpen, description = it.summary, icon = it.icon) }
            }
        } else {
            val found = index.search(query)
            if (found.isEmpty()) PageNote("No matching settings")
            Column { found.forEach { SettingsResultRow(it) } }
        }
    }
}
