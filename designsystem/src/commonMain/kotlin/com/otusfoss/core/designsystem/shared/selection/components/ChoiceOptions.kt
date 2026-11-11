package com.otusfoss.core.designsystem.shared.selection.components

/**
 * What a [ChoiceSheet] offers: every option, the [selected] one, and an optional [default], such
 * as "Use device format", which is listed first in a group of its own.
 */
public data class ChoiceOptions<T>(
    val options: List<T>,
    val selected: T,
    val default: T? = null,
) {
    /** The groups the sheet shows, in order: the default alone, then everything else. */
    internal val groups: List<List<T>>
        get() =
            if (default == null || default !in options) {
                listOf(options)
            } else {
                listOf(listOf(default), options - default).filter { it.isNotEmpty() }
            }
}
