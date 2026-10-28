package com.otus.settings.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.otus.designsystem.shared.rows.components.Collapsible
import com.otus.designsystem.shared.settings.components.SettingRow
import com.otus.designsystem.shared.settings.elements.SwatchStack
import com.otus.designsystem.shared.settings.elements.ValueButton
import com.otus.designsystem.theme.colors.ThemeChoice
import com.otus.designsystem.theme.colors.ThemeColor
import com.otus.designsystem.theme.colors.get
import com.otus.designsystem.theme.colors.toHex
import com.otus.settings.presentation.description
import com.otus.settings.presentation.title

/**
 * A row showing the main colors of [choice]'s theme that unfolds all of them while [expanded];
 * [onEdit] opens one. On a built-in theme it says a change makes a custom theme; on a custom theme
 * it counts the changed colors.
 */
@Composable
internal fun ThemeColorRows(
    choice: ThemeChoice,
    expanded: Boolean,
    onExpand: () -> Unit,
    onEdit: (ThemeColor) -> Unit,
) {
    val palette = choice.palette
    val custom = choice.customTheme
    SettingRow(
        "Customize colors",
        onExpand,
        description =
            if (custom == null) {
                "Changes make a custom theme"
            } else {
                "${custom.colors.size} changed from ${custom.base.title}"
            },
    ) { SwatchStack(stacked.map { palette[it] }) }
    Collapsible(expanded) {
        Column {
            ThemeColor.entries.forEach { color ->
                SettingRow(color.title, { onEdit(color) }, description = color.description) {
                    ValueButton(palette[color].toHex(), fill = palette[color])
                }
            }
        }
    }
}

/** The colors that set a theme apart at a glance. */
private val stacked =
    listOf(ThemeColor.Page, ThemeColor.Raised, ThemeColor.Controls, ThemeColor.Text, ThemeColor.AccentOutline)
