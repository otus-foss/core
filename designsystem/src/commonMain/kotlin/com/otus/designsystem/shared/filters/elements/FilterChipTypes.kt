package com.otus.designsystem.shared.filters.elements

/** How a [FilterChip] reads to accessibility services, and how it toggles. */
public enum class FilterChipRole {
    /** One of several choices that can be on together. */
    Checkbox,

    /** One choice of a group where only one is on. */
    Radio,

    /** Opens something, such as a list of every folder; [FilterChip]'s `selected` is ignored. */
    Button,
}

/**
 * What a [FilterChip] says: its [text], an optional `text3` [prefix] before it, such as the parent
 * folders of a folder whose name repeats, and what screen readers hear, by default both, such as
 * a folder's full path.
 */
public data class ChipLabel(
    val text: String,
    val prefix: String? = null,
    val spoken: String = listOfNotNull(prefix, text).joinToString(", "),
)
