package com.otus.designsystem.components

import org.jetbrains.compose.resources.DrawableResource

/** One line of an [OptionsMenu]. */
public sealed interface OptionsItem {
    /**
     * Runs [onClick] and closes the menu; one that [opens] something more shows a chevron. A
     * [detail], such as "Usually Invoices", shows as a smaller second line.
     */
    public class Action(
        public val label: String,
        public val icon: DrawableResource,
        public val onClick: () -> Unit,
        public val opens: Boolean = false,
        public val detail: String? = null,
    ) : OptionsItem

    /**
     * Turns an option on or off with a switch, and leaves the menu open. A [detail] shows as a
     * smaller second line.
     */
    public class Toggle(
        public val label: String,
        public val icon: DrawableResource,
        public val checked: Boolean,
        public val detail: String?,
        public val onCheckedChange: (Boolean) -> Unit,
    ) : OptionsItem {
        /** A toggle without a second line, so the change can still be the trailing lambda. */
        public constructor(
            label: String,
            icon: DrawableResource,
            checked: Boolean,
            onCheckedChange: (Boolean) -> Unit,
        ) : this(label, icon, checked, null, onCheckedChange)
    }

    /** A small `text3` heading over the items after it, such as the sender's name. */
    public class Caption(
        public val text: String,
    ) : OptionsItem

    /** A line between groups of items. */
    public data object Divider : OptionsItem
}
