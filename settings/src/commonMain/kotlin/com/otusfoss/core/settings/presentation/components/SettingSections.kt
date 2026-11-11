package com.otusfoss.core.settings.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.shared.rows.components.RowDensity
import com.otusfoss.core.designsystem.shared.settings.components.DependentSettings
import com.otusfoss.core.designsystem.shared.settings.components.LocalToggleRowDensity
import com.otusfoss.core.designsystem.shared.settings.components.SettingPageRow
import com.otusfoss.core.designsystem.shared.settings.components.SettingRow
import com.otusfoss.core.designsystem.shared.settings.components.SettingToggleRow
import com.otusfoss.core.designsystem.shared.settings.components.SettingsSection
import com.otusfoss.core.designsystem.shared.settings.elements.ValueButton

/** Shows [sections] in order, each under its heading; a section without rows does not show. */
@Composable
public fun SettingSections(sections: List<SettingsSectionSpec>) {
    sections.filter { it.rows.isNotEmpty() }.forEach { section ->
        when (val title = section.title) {
            null -> SpecRows(section.rows)
            else -> SettingsSection(title, info = section.info) { SpecRows(section.rows) }
        }
    }
}

@Composable
private fun SpecRows(rows: List<SettingRowSpec>) {
    rows.forEach { key(it.title) { SettingSpecRow(it) } }
}

/**
 * Shows [row]: a switch, a value that opens its sheet, or a chevron that opens its page. While the
 * row is unavailable it stays in place, dimmed, and names what it waits on. A compact [density]
 * makes switches and values shorter, for a list inside a card.
 */
@Composable
public fun SettingSpecRow(
    row: SettingRowSpec,
    modifier: Modifier = Modifier,
    density: RowDensity = RowDensity.Regular,
) {
    SpecRow(row, modifier, waiting = null, density)
}

@Composable
private fun SpecRow(
    row: SettingRowSpec,
    modifier: Modifier,
    waiting: String?,
    density: RowDensity,
) {
    when (row) {
        is ToggleRowSpec -> ToggleSpecRow(row, modifier, waiting, density)
        is ChoiceRowSpec -> ChoiceSpecRow(row, modifier, waiting, density)
        is PageRowSpec -> PageSpecRow(row, modifier, waiting)
        is WaitingRowSpec -> SpecRow(row.row, modifier, row.reason, density)
    }
}

@Composable
private fun ToggleSpecRow(
    row: ToggleRowSpec,
    modifier: Modifier,
    waiting: String?,
    density: RowDensity,
) {
    DependentSettings(enabled = waiting == null) {
        CompositionLocalProvider(LocalToggleRowDensity provides density) {
            SettingToggleRow(
                row.title,
                row.checked,
                row.onCheckedChange,
                modifier,
                description = waiting ?: row.description,
                icon = row.icon,
                info = row.info,
            )
        }
    }
}

@Composable
private fun ChoiceSpecRow(
    row: ChoiceRowSpec,
    modifier: Modifier,
    waiting: String?,
    density: RowDensity,
) {
    var choosing by rememberSaveable { mutableStateOf(false) }
    DependentSettings(enabled = waiting == null) {
        SettingRow(row.title, { choosing = true }, modifier, waiting ?: row.description, row.icon, density) {
            ValueButton(row.value)
        }
    }
    if (choosing) row.sheet { choosing = false }
}

@Composable
private fun PageSpecRow(
    row: PageRowSpec,
    modifier: Modifier,
    waiting: String?,
) {
    DependentSettings(enabled = waiting == null) {
        SettingPageRow(row.title, { row.open(null) }, modifier, waiting ?: row.value ?: row.description, row.icon)
    }
}
