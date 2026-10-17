package com.otus.designsystem.components

import org.jetbrains.compose.resources.DrawableResource

/** One line of an [OptionsMenu]. */
public sealed interface OptionsItem {
    /** Runs [onClick] and closes the menu; one that [opens] something more shows a chevron. */
    public class Action(
        public val label: String,
        public val icon: DrawableResource,
        public val onClick: () -> Unit,
        public val opens: Boolean = false,
    ) : OptionsItem

    /** Turns an option on or off with a switch, and leaves the menu open. */
    public class Toggle(
        public val label: String,
        public val icon: DrawableResource,
        public val checked: Boolean,
        public val onCheckedChange: (Boolean) -> Unit,
    ) : OptionsItem

    /** A line between groups of items. */
    public data object Divider : OptionsItem
}
