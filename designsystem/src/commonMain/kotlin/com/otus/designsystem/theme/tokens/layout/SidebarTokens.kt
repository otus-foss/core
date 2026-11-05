package com.otus.designsystem.theme.tokens.layout

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otus.designsystem.theme.tokens.selection.FilterTokens

internal object SidebarTokens {
    /** The page stays visible beyond it. */
    val width = 336.dp

    /** On a narrow screen the sidebar shrinks so at least this much of the page stays visible. */
    val pageVisible = 56.dp
    val inset = 8.dp
    val entryPadding = 12.dp
    val entryRadius = CornerTokens.row

    /** The expand button at the end of a folder row with subfolders. */
    val expandButton = 32.dp
    val headingChevron = 16.dp

    /** Headings, counts, and the subtitle under the title. */
    val detailSize = 14.sp
    val detailLineHeight = 18.sp

    /** Each nesting level moves an entry's icon and name this far toward the trailing edge. */
    val nestIndent = 16.dp
    const val SCRIM_ALPHA = 0.65f

    /** The avatars before the header's title. */
    val headerAvatar = 32.dp

    /** The large row for the main mailbox, such as Inbox. */
    val inboxHeight = 52.dp

    /** One mailbox in the grid: its icon over a short label. */
    val tileHeight = 64.dp
    const val TILE_COLUMNS = 3
    val tileLabelSize = 12.sp
    val tileLabelLineHeight = 16.sp
    val tileLabelGap = 4.dp

    /**
     * A count on a tile's icon or the inset Inbox row, ringed in the surface color so it stands clear.
     * The ring sits outside a fill the size of the filter button's count, so both counts read alike.
     */
    val badgeRing = 2.dp
    val badgeHeight = FilterTokens.badgeSize + badgeRing * 2
    val badgePadding = 4.dp + badgeRing
    val badgeSize = FilterTokens.badgeTextSize

    /** The gap between mailbox tiles. */
    val tileGap = 8.dp

    /** In the tile editor, the minus on a tile's top-start corner and its icon. */
    val removeBadge = 22.dp
    val removeIcon = 16.dp

    /** In the tile editor, the arc on a tile's bottom-end corner that resizes it. */
    val resizeArc = 10.dp
    val resizeStroke = 3.dp
    val resizeInset = 8.dp

    /** In the tile editor, a lifted tile grows by this factor. */
    const val LIFTED_SCALE = 1.04f

    /** The owl in the footer, at the sidebar's bottom edge. */
    val owlHeight = 48.dp

    /**
     * Space under the footer owl. The art leaves more room below the owl's feet than beside its
     * body, so this is less than the leading inset, and the owl's body sits as far from the bottom
     * as from the leading edge.
     */
    val owlBottomGap = 5.dp

    /**
     * The circled arrow before the version in the footer when a newer build is out. While the
     * update downloads, a ring of [updateRing] fills around the circle.
     */
    val updateBadge = 20.dp
    val updateIcon = 14.dp
    val updateRing = 2.dp

    /** A swipe released faster than this, per second, opens or closes the sidebar regardless of position. */
    val flingVelocity = 400.dp

    /** How far a section or folder chevron turns when it opens. */
    const val CHEVRON_OPEN_DEGREES = 90f
}
