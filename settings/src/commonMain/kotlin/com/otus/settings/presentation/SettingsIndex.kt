package com.otus.settings.presentation

import org.jetbrains.compose.resources.DrawableResource

/**
 * One thing settings search finds: a setting, or a page that [opens] one. It lives on [page],
 * which is null for a top-level page. [detail] is the section it sits under, or a top-level page's
 * summary. [options] are the choices it offers, so a search for one finds the setting.
 */
public class SettingsItem internal constructor(
    public val title: String,
    public val page: SettingsPage?,
    public val detail: String?,
    public val icon: DrawableResource?,
    public val value: String?,
    public val options: List<String>,
    public val opens: SettingsPage?,
) {
    public val isPage: Boolean get() = opens != null

    /** Opens the page this item opens at its top, or this setting's page with the setting found. */
    public fun onOpen() {
        opens?.onOpen() ?: page?.open(title)
    }
}

/**
 * A settings page by its [path] of page titles from the settings home, with its icon. [open] shows
 * it with the named setting found, or at its top for null.
 */
public class SettingsPage internal constructor(
    public val path: List<String>,
    public val icon: DrawableResource?,
    internal val open: (setting: String?) -> Unit,
) {
    public fun onOpen() {
        open(null)
    }
}

@DslMarker
internal annotation class SettingsIndexDsl

/** Builds the settings index: top-level pages, each holding its settings and subpages. */
public fun settingsIndex(build: SettingsIndexScope.() -> Unit): List<SettingsItem> =
    buildList { SettingsIndexScope(page = null, items = this).build() }

/**
 * A page while the index is built, or the settings home for top-level pages. Its settings open it
 * on themselves and use its icon unless they name their own.
 */
@SettingsIndexDsl
public class SettingsIndexScope internal constructor(
    private val page: SettingsPage?,
    private val items: MutableList<SettingsItem>,
) {
    private val icon = page?.icon
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
        val child = SettingsPage(page?.path.orEmpty() + title, icon, open)
        items += SettingsItem(title, page, section ?: summary, icon, value, options, opens = child)
        SettingsIndexScope(child, items).content()
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
        items += SettingsItem(title, page, section, icon, value, options, opens = null)
    }

    /** A setting that turns something on or off. */
    public fun toggle(
        title: String,
        on: Boolean,
        icon: DrawableResource? = this.icon,
    ) {
        setting(title, if (on) "On" else "Off", icon = icon)
    }
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
        page?.path.orEmpty().any { it.has() } || detail?.has() == true -> 2
        else -> null
    }
}
