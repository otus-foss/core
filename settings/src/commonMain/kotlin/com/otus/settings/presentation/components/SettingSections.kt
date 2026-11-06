package com.otus.settings.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.otus.designsystem.shared.settings.components.DependentSettings
import com.otus.designsystem.shared.settings.components.SettingPageRow
import com.otus.designsystem.shared.settings.components.SettingRow
import com.otus.designsystem.shared.settings.components.SettingToggleRow
import com.otus.designsystem.shared.settings.components.SettingsSection
import com.otus.designsystem.shared.settings.elements.ValueButton

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
 * row is unavailable it stays in place, dimmed, and names what it waits on.
 */
@Composable
public fun SettingSpecRow(
    row: SettingRowSpec,
    modifier: Modifier = Modifier,
) {
    when (row) {
        is ToggleRowSpec -> ToggleSpecRow(row, modifier)
        is ChoiceRowSpec -> ChoiceSpecRow(row, modifier)
        is PageRowSpec -> PageSpecRow(row, modifier)
    }
}

@Composable
private fun ToggleSpecRow(
    row: ToggleRowSpec,
    modifier: Modifier,
) {
    DependentSettings(enabled = row.unavailable == null) {
        SettingToggleRow(
            row.title,
            row.checked,
            row.onCheckedChange,
            modifier,
            description = row.shownDescription,
            icon = row.icon,
            info = row.info,
        )
    }
}

@Composable
private fun ChoiceSpecRow(
    row: ChoiceRowSpec,
    modifier: Modifier,
) {
    var choosing by rememberSaveable { mutableStateOf(false) }
    DependentSettings(enabled = row.unavailable == null) {
        SettingRow(row.title, { choosing = true }, modifier, row.shownDescription, row.icon) { ValueButton(row.value) }
    }
    if (choosing) row.sheet { choosing = false }
}

@Composable
private fun PageSpecRow(
    row: PageRowSpec,
    modifier: Modifier,
) {
    DependentSettings(enabled = row.unavailable == null) {
        val description = row.unavailable ?: row.value ?: row.description
        SettingPageRow(row.title, { row.open(null) }, modifier, description, row.icon)
    }
}

private val SettingRowSpec.shownDescription: String? get() = unavailable ?: description
