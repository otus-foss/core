package com.otus.settings.presentation.screens

import androidx.compose.runtime.Composable
import com.otus.designsystem.components.PageScaffold
import com.otus.designsystem.elements.ActionRow
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
        entries.forEach { entry ->
            ActionRow(
                entry.title,
                entry.onOpen,
                Res.drawable.symbol_chevron_right,
                summary = entry.summary,
                icon = entry.icon,
                mirrorTrailingInRtl = true,
            )
        }
    }
}
