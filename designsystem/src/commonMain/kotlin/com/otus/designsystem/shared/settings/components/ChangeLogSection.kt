package com.otus.designsystem.shared.settings.components

/** One part of the app in a [ChangeLogSheet], such as "Sidebar", and the [changes] made to it. */
public data class ChangeLogSection(
    val title: String,
    val changes: List<String>,
)

/**
 * One published build in a [ChangeLogHistorySheet]: its [title], such as its date, what changed in
 * it, and whether the running app already has it.
 */
public data class ChangeLogRelease(
    val title: String,
    val sections: List<ChangeLogSection>,
    val installed: Boolean = false,
)
