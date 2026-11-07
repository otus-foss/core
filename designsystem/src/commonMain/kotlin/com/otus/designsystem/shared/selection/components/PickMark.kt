package com.otus.designsystem.shared.selection.components

/**
 * The mark of a [PickRow]. Screen readers hear a checkbox when several may be [multiple] and a radio
 * button otherwise. The mark ends the row unless [atStart], as in a list of choices with long
 * descriptions, where the radio leads. [style] says how it is drawn.
 */
public data class PickMark(
    val selected: Boolean,
    val multiple: Boolean = false,
    val atStart: Boolean = false,
    val style: PickStyle = PickStyle.Mark,
)

/** How a [PickMark] is drawn. */
public enum class PickStyle {
    /** A radio, or a checkbox when [PickMark.multiple]. */
    Mark,

    /** A circle that fills with a check while selected. */
    CheckCircle,

    /** A check beside the selected option only, with room kept for it on every row. */
    Check,

    /**
     * A rounded card that fills with `selectedFill` while selected, with a checkbox when
     * [PickMark.multiple] and no mark otherwise, for rows in a menu or a padded list.
     */
    Card,
}
