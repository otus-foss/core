package com.otus.designsystem.components

/**
 * The mark of a [PickRow]: a radio for one choice, a checkbox when several may be [multiple]. It
 * ends the row unless [atStart], as in a settings list of choices with long descriptions, where
 * the radio leads.
 */
public data class PickMark(
    val selected: Boolean,
    val multiple: Boolean = false,
    val atStart: Boolean = false,
)
