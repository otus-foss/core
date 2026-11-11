package com.otusfoss.core.designsystem.mail.sidebar.elements

import com.otusfoss.core.designsystem.theme.tokens.layout.SidebarTokens

/** How many of a SidebarMailboxGrid's three columns a [SidebarMailboxTile] spans. */
public enum class SidebarTileSize(
    public val columns: Int,
) {
    /** The icon over a short label. */
    Small(1),

    /** The icon, label, and count in one line. */
    Wide(2),

    /** A Wide tile across the whole grid. */
    Full(SidebarTokens.TILE_COLUMNS),
}
