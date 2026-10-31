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
import com.otus.settings.presentation.SettingsEntry

/** The settings home: one row per page, which a search narrows by title and summary. */
@Composable
public fun SettingsScreen(
    entries: List<SettingsEntry>,
    onOpenNavigation: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val shown = entries.filter { it.matches(query) }
    PageScaffold(
        "Settings",
        navigationLabel = "Open navigation",
        onNavigation = onOpenNavigation,
        search = PageSearch(query, { query = it }, "Search settings"),
    ) {
        if (shown.isEmpty()) PageNote("No matching settings")
        Column {
            shown.forEach { entry ->
                SettingPageRow(entry.title, entry.onOpen, description = entry.summary, icon = entry.icon)
            }
        }
    }
}

private fun SettingsEntry.matches(query: String): Boolean =
    query.isBlank() || listOf(title, summary).any { it.contains(query.trim(), ignoreCase = true) }
