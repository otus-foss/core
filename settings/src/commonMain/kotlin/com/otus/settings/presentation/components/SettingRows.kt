package com.otus.settings.presentation.components

import androidx.compose.runtime.Composable
import com.otus.designsystem.shared.feedback.components.InfoText
import com.otus.settings.presentation.SettingsIndexScope
import org.jetbrains.compose.resources.DrawableResource

/**
 * A page's rows under the heading [title], or straight on the page for null, with [info]
 * explaining the heading. A page declares its rows once as these values: its screen shows them
 * with [SettingSections], and settings search lists them with [sections].
 */
public class SettingsSectionSpec(
    public val title: String?,
    public val rows: List<SettingRowSpec>,
    public val info: InfoText? = null,
)

/**
 * One row of a settings page, as the page shows it and search finds it. The row shows [description]
 * under its title; [keywords] are other words search finds it by. [waitingOn] dims a row that waits
 * on another setting.
 */
public sealed interface SettingRowSpec {
    public val title: String
    public val icon: DrawableResource?
    public val description: String?
    public val keywords: List<String>
}

/** A row that turns something on or off; [info] explains it on request. */
public class ToggleRowSpec(
    override val title: String,
    public val checked: Boolean,
    public val onCheckedChange: (Boolean) -> Unit,
    override val icon: DrawableResource? = null,
    override val description: String? = null,
    public val info: InfoText? = null,
    override val keywords: List<String> = emptyList(),
) : SettingRowSpec

/**
 * A row showing its [value] that opens [sheet] to change it, which calls its `onDismiss` to close.
 * [options] are the values the sheet offers, which search finds the row by.
 */
public class ChoiceRowSpec(
    override val title: String,
    public val value: String,
    public val sheet: @Composable (onDismiss: () -> Unit) -> Unit,
    public val options: List<String> = emptyList(),
    override val icon: DrawableResource? = null,
    override val description: String? = null,
    override val keywords: List<String> = emptyList(),
) : SettingRowSpec

/**
 * A row that [open]s a page, at its top for null or with the named setting found. The row shows the
 * page's current [value], or else its [description]. [content] lists the page's own rows for search.
 */
public class PageRowSpec(
    override val title: String,
    public val open: (setting: String?) -> Unit,
    public val value: String? = null,
    override val description: String? = null,
    override val icon: DrawableResource? = null,
    override val keywords: List<String> = emptyList(),
    public val content: () -> List<SettingsSectionSpec> = { emptyList() },
) : SettingRowSpec

/** Lists [sections] for search as their page shows them, each under its heading. */
public fun SettingsIndexScope.sections(sections: List<SettingsSectionSpec>) {
    sections.forEach { spec ->
        when (val title = spec.title) {
            null -> spec.rows.forEach { row(it) }
            else -> section(title) { spec.rows.forEach { row(it) } }
        }
    }
}

/** [row], dimmed in place while it waits on another setting, with [reason] in place of its description. */
public class WaitingRowSpec(
    public val row: SettingRowSpec,
    public val reason: String,
) : SettingRowSpec by row

/** This row waiting on another setting while [reason] is not null, or else the row itself. */
public fun SettingRowSpec.waitingOn(reason: String?): SettingRowSpec = reason?.let { WaitingRowSpec(this, it) } ?: this

/** Lists [row] for search, with the reason it waits on while it is unavailable. */
public fun SettingsIndexScope.row(row: SettingRowSpec) {
    when (row) {
        is ToggleRowSpec -> toggle(row.title, row.checked, row.icon, row.keywords)
        is ChoiceRowSpec -> setting(row.title, row.value, row.options, row.icon, row.keywords)
        is PageRowSpec -> pageRow(row)
        is WaitingRowSpec -> unavailable(row.reason) { this.row(row.row) }
    }
}

private fun SettingsIndexScope.pageRow(row: PageRowSpec) {
    page(row.title, row.open, row.icon, summary = row.description, value = row.value) {
        keywords(row.keywords)
        sections(row.content())
    }
}
