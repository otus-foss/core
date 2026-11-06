package com.otus.settings.presentation

import org.jetbrains.compose.resources.DrawableResource

/**
 * One thing settings search finds: a setting, or a page that [opens] one. It lives on [page],
 * which is null for a top-level page. [detail] is the section it sits under, or a top-level page's
 * summary. [options] are the choices it offers and [keywords] other words people use for it, so a
 * search for either finds the setting. [unavailable] says what it waits on while its page shows it
 * disabled, such as another setting that has to be on first.
 */
public class SettingsItem internal constructor(
    public val title: String,
    public val page: SettingsPage?,
    public val detail: String?,
    public val icon: DrawableResource?,
    private val facts: ItemFacts,
    public val opens: SettingsPage?,
) {
    public val value: String? get() = facts.value
    public val options: List<String> get() = facts.options
    public val keywords: List<String> get() = facts.keywords
    public val unavailable: String? get() = facts.unavailable

    public val isPage: Boolean get() = opens != null

    /** Opens the page this item opens at its top, or this setting's page with the setting found. */
    public fun onOpen() {
        opens?.onOpen() ?: page?.open(title)
    }
}

/** What an item shows and what else search matches it by. */
internal class ItemFacts(
    val value: String?,
    val options: List<String>,
    val keywords: List<String>,
    val unavailable: String?,
)

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
 * on themselves and use its icon unless they name their own; a null icon names none.
 */
@SettingsIndexDsl
public class SettingsIndexScope internal constructor(
    private val page: SettingsPage?,
    private val items: MutableList<SettingsItem>,
) {
    private val icon = page?.icon
    private var section: String? = null
    private var waiting: String? = null
    private val pageKeywords = mutableListOf<String>()

    /**
     * A page under this one, which [open] shows at its top for null or with the named setting
     * found. Its [content] lists what the page holds, and may name other words for the page with
     * [keywords].
     */
    public fun page(
        title: String,
        open: (setting: String?) -> Unit,
        icon: DrawableResource? = null,
        summary: String? = null,
        value: String? = null,
        options: List<String> = emptyList(),
        content: SettingsIndexScope.() -> Unit = {},
    ) {
        val child = SettingsPage(page?.path.orEmpty() + title, icon ?: this.icon, open)
        val inside = SettingsIndexScope(child, mutableListOf()).apply(content)
        val facts = ItemFacts(value, options, inside.pageKeywords, waiting)
        items += SettingsItem(title, page, section ?: summary, child.icon, facts, opens = child)
        items += inside.items
    }

    /** Other words people use for this page, which search matches as it matches options. */
    public fun keywords(vararg words: String) {
        pageKeywords += words
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

    /**
     * Settings and pages that this page shows disabled while they wait on another setting, which
     * [reason] names; null while they are available. Search shows the reason with each one.
     */
    public fun unavailable(
        reason: String?,
        content: SettingsIndexScope.() -> Unit,
    ) {
        val outer = waiting
        waiting = reason ?: outer
        content()
        waiting = outer
    }

    /** A setting showing [value], one of its [options] if it offers a choice; [keywords] are other words for it. */
    public fun setting(
        title: String,
        value: String? = null,
        options: List<String> = emptyList(),
        icon: DrawableResource? = null,
        keywords: List<String> = emptyList(),
    ) {
        val facts = ItemFacts(value, options, keywords, waiting)
        items += SettingsItem(title, page, section, icon ?: this.icon, facts, opens = null)
    }

    /** A setting that turns something on or off, with [keywords] as [setting] takes them. */
    public fun toggle(
        title: String,
        on: Boolean,
        icon: DrawableResource? = null,
        keywords: List<String> = emptyList(),
    ) {
        setting(title, if (on) "On" else "Off", icon = icon, keywords = keywords)
    }
}

/**
 * The items matching [query], best first: by name, then by one of their options or keywords, then
 * by path. A query of several words that no field holds whole, such as "swipe archive", still
 * finds an item that holds every word somewhere, after all of those.
 */
public fun List<SettingsItem>.search(query: String): List<SettingsItem> {
    val wanted = query.trim()
    if (wanted.isEmpty()) return emptyList()
    val words = wanted.split(Whitespace)
    return mapNotNull { item -> item.match(wanted, words)?.let { item to it } }
        .sortedBy { (_, match) -> match }
        .map { (item, _) -> item }
}

/** How an item matches a query, best first. */
private enum class Match { Title, Named, Placed, EveryWord }

private fun SettingsItem.match(
    query: String,
    words: List<String>,
): Match? {
    val named = options + keywords
    val placed = page?.path.orEmpty() + listOfNotNull(detail)
    val every = listOf(title) + named + placed
    return when {
        title.has(query) -> Match.Title
        named.any { it.has(query) } -> Match.Named
        placed.any { it.has(query) } -> Match.Placed
        words.size > 1 && words.all { word -> every.any { it.has(word) } } -> Match.EveryWord
        else -> null
    }
}

private fun String.has(query: String) = contains(query, ignoreCase = true)

private val Whitespace = Regex("\\s+")
