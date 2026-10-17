package com.otus.designsystem.components

/** The mark at the end of a [PickRow]: a radio for one choice, a checkbox when several may be [multiple]. */
public data class PickMark(
    val selected: Boolean,
    val multiple: Boolean = false,
)
