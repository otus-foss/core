package com.otusfoss.core.designsystem.shared.people.components

import org.jetbrains.compose.resources.DrawableResource

/** One tile in [PersonTiles]: its [label] under a symbol or a number, and what a tap does. */
public sealed interface PersonTile {
    public val label: String
    public val onClick: () -> Unit

    /** A way to reach the person, such as Write; the one [primary] tile takes the accent. */
    public class Action(
        override val label: String,
        public val icon: DrawableResource,
        override val onClick: () -> Unit,
        public val primary: Boolean = false,
    ) : PersonTile

    /** How much mail there is, such as "48 Messages"; a tap shows that mail. */
    public class Count(
        override val label: String,
        public val count: Long,
        override val onClick: () -> Unit,
    ) : PersonTile
}
