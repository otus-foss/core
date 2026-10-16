package com.otus.designsystem.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object SidebarTokens {
    /** The page stays visible beyond it. */
    val width = 336.dp

    /** On a narrow screen the sidebar shrinks so at least this much of the page stays visible. */
    val pageVisible = 56.dp
    val inset = 8.dp
    val entryPadding = 12.dp
    val entryRadius = 16.dp

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

    /** A count on a tile's icon, ringed in the surface color so it stands clear of the icon. */
    val badgeHeight = 16.dp
    val badgePadding = 4.dp
    val badgeRing = 2.dp
    val badgeSize = 10.sp

    /** A swipe released faster than this, per second, opens or closes the sidebar regardless of position. */
    val flingVelocity = 400.dp

    /** How far a section or folder chevron turns when it opens. */
    const val CHEVRON_OPEN_DEGREES = 90f
}
