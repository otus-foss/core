package com.otusfoss.core.settings.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.otusfoss.core.designsystem.shared.rows.components.Collapsible
import com.otusfoss.core.designsystem.shared.settings.components.SettingRow
import com.otusfoss.core.designsystem.shared.settings.elements.SwatchStack
import com.otusfoss.core.designsystem.shared.settings.elements.ValueButton
import com.otusfoss.core.designsystem.theme.colors.OtusPalette
import com.otusfoss.core.designsystem.theme.colors.ThemeChoice
import com.otusfoss.core.designsystem.theme.colors.ThemeColor
import com.otusfoss.core.designsystem.theme.colors.get
import com.otusfoss.core.designsystem.theme.colors.toHex
import com.otusfoss.core.settings.presentation.description
import com.otusfoss.core.settings.presentation.title

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
                ThemeColorRow(color, palette) { onEdit(color) }
            }
        }
    }
}

@Composable
private fun ThemeColorRow(
    color: ThemeColor,
    palette: OtusPalette,
    onEdit: () -> Unit,
) {
    SettingRow(color.title, onEdit, description = color.description) {
        ValueButton(palette[color].toHex(), fill = palette[color])
    }
}

/** The colors that set a theme apart at a glance. */
private val stacked =
    listOf(ThemeColor.Page, ThemeColor.Raised, ThemeColor.Controls, ThemeColor.Text, ThemeColor.AccentOutline)
