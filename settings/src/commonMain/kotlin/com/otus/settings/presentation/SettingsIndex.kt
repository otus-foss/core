package com.otus.settings.presentation

import org.jetbrains.compose.resources.DrawableResource

/**
 * One thing settings search finds: a page, or a setting on one. [path] names the pages above it
 * and then its section, as the result shows it. [options] are the choices it offers, so a search
 * for one finds the setting. [summary] describes a top-level page on the settings home.
 */
public class SettingsItem internal constructor(
    public val title: String,
    public val path: List<String>,
    public val icon: DrawableResource?,
    public val value: String?,
    public val options: List<String>,
    public val isPage: Boolean,
    public val summary: String?,
    public val onOpen: () -> Unit,
)

@DslMarker
internal annotation class SettingsIndexDsl

/** Builds the settings index: top-level pages, each holding its settings and subpages. */
public fun settingsIndex(build: SettingsIndexScope.() -> Unit): List<SettingsItem> =
    buildList { SettingsIndexScope(emptyList(), icon = null, open = {}, items = this).build() }

/**
 * A page while the index is built. Its settings open it on themselves, take its path, and use its
 * icon unless they name their own.
 */
@SettingsIndexDsl
public class SettingsIndexScope internal constructor(
    private val path: List<String>,
    private val icon: DrawableResource?,
    private val open: (setting: String?) -> Unit,
    private val items: MutableList<SettingsItem>,
) {
    private var section: String? = null

    /**
     * A page under this one, which [open] shows at its top for null or with the named setting
     * found. Its [content] lists what the page holds.
     */
    public fun page(
        title: String,
        open: (setting: String?) -> Unit,
        icon: DrawableResource? = this.icon,
        summary: String? = null,
        value: String? = null,
        options: List<String> = emptyList(),
        content: SettingsIndexScope.() -> Unit = {},
    ) {
        items += SettingsItem(title, here(), icon, value, options, isPage = true, summary) { open(null) }
        SettingsIndexScope(path + title, icon, open, items).content()
    }

    /** The settings under the heading [title] on this page. */
    public fun section(
        title: String,
        content: SettingsIndexScope.() -> Unit,
    ) {
        val outer = section
        section = title
        content()
        section = outer
    }

    /** A setting showing [value], one of its [options] if it offers a choice. */
    public fun setting(
        title: String,
        value: String? = null,
        options: List<String> = emptyList(),
        icon: DrawableResource? = this.icon,
    ) {
        items += SettingsItem(title, here(), icon, value, options, isPage = false, summary = null) { open(title) }
    }

    /** A setting that turns something on or off. */
    public fun toggle(
        title: String,
        on: Boolean,
        icon: DrawableResource? = this.icon,
    ) {
        setting(title, if (on) "On" else "Off", icon = icon)
    }

    private fun here(): List<String> = path + listOfNotNull(section)
}

/** The items matching [query], best first: by name, then by one of their options, then by path. */
public fun List<SettingsItem>.search(query: String): List<SettingsItem> {
    val wanted = query.trim()
    if (wanted.isEmpty()) return emptyList()
    return mapNotNull { item -> item.rank(wanted)?.let { item to it } }
        .sortedBy { (_, rank) -> rank }
        .map { (item, _) -> item }
}

private fun SettingsItem.rank(query: String): Int? {
    fun String.has() = contains(query, ignoreCase = true)
    return when {
        title.has() -> 0
        options.any { it.has() } -> 1
        path.any { it.has() } || summary?.has() == true -> 2
        else -> null
    }
}
