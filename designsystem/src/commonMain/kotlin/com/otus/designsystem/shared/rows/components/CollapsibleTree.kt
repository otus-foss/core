package com.otus.designsystem.shared.rows.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * [items] in tree order, where the items nested under one follow it with a greater [depth]. An
 * item with nested items can collapse them: [row] receives whether it is expanded and a callback
 * that changes it, or null for an item with nothing nested. Everything starts expanded, and
 * [expandAll], such as while a search narrows the list, shows every item without chevrons.
 */
@Composable
public fun <T> CollapsibleTree(
    items: List<T>,
    id: (T) -> String,
    depth: (T) -> Int,
    modifier: Modifier = Modifier,
    expandAll: Boolean = false,
    row: @Composable (item: T, expanded: Boolean?, onExpandedChange: (Boolean) -> Unit) -> Unit,
) {
    var collapsed by rememberSaveable(stateSaver = IdsSaver) { mutableStateOf(emptySet<String>()) }
    val entries = items.map { TreeEntry(id(it), depth(it)) }
    val parents = if (expandAll) emptySet() else entries.parentIds()
    val hidden = if (expandAll) emptySet() else entries.collapsedAway(collapsed)
    Column(modifier) {
        items.zip(entries).forEach { (item, entry) ->
            Collapsible(entry.id !in hidden) {
                row(item, (entry.id !in collapsed).takeIf { entry.id in parents }) { expand ->
                    collapsed = if (expand) collapsed - entry.id else collapsed + entry.id
                }
            }
        }
    }
}

internal data class TreeEntry(
    val id: String,
    val depth: Int,
)

/** The ids of entries that have nested entries right after them. */
internal fun List<TreeEntry>.parentIds(): Set<String> =
    zipWithNext().filter { (entry, next) -> next.depth > entry.depth }.map { it.first.id }.toSet()

/** The ids of entries hidden because an entry above them is [collapsed]. */
internal fun List<TreeEntry>.collapsedAway(collapsed: Set<String>): Set<String> =
    buildSet {
        // The depth of the collapsed entry whose subtree is being skipped, if any.
        var hidingBelow: Int? = null
        this@collapsedAway.forEach { entry ->
            if (entry.depth > (hidingBelow ?: Int.MAX_VALUE)) {
                add(entry.id)
            } else {
                hidingBelow = entry.depth.takeIf { entry.id in collapsed }
            }
        }
    }

private val IdsSaver = listSaver<Set<String>, String>(save = { it.toList() }, restore = { it.toSet() })
