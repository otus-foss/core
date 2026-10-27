package com.otus.designsystem.shared.filters.components

/** A folder's checkbox in a [FilterFolderRow]. */
public enum class FolderCheck {
    Off,
    On,

    /** Checked because a parent is checked with its subfolders; it cannot change on its own. */
    Included,
}

/** A folder in a [FilterFolderRow]: its [name], how deep it nests, and its parents' [path]. */
public data class TreeFolder(
    val name: String,
    val depth: Int = 0,
    val path: String? = null,
)
