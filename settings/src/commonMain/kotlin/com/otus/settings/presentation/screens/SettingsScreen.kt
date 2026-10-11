package com.otus.settings.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.otus.designsystem.components.PageScaffold
import com.otus.designsystem.components.SettingRow
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_right
import com.otus.settings.presentation.SettingsEntry

/** The top settings page: one row per subpage, in the order the app lists them. */
@Composable
public fun SettingsScreen(
    entries: List<SettingsEntry>,
    onOpenNavigation: () -> Unit,
) {
    PageScaffold("Settings", navigationLabel = "Open navigation", onNavigation = onOpenNavigation) {
        Column {
            entries.forEach { entry ->
                SettingRow(entry.title, entry.onOpen, description = entry.summary, icon = entry.icon) {
                    Symbol(Res.drawable.symbol_chevron_right, mirrorInRtl = true)
                }
            }
        }
    }
}
