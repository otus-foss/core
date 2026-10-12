package com.otus.designsystem.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object SidebarTokens {
    /** The account rail along the leading edge. */
    val railWidth = 64.dp

    /** The panel beside the rail; the page stays visible beyond it. */
    val panelWidth = 272.dp
    val width = railWidth + panelWidth

    /** On a narrow screen the panel shrinks so at least this much of the page stays visible. */
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

    val railAvatar = 40.dp
    val railGap = 4.dp

    /** The selected rail item's outline, drawn this far outside its circle. */
    val selectionOutline = 2.dp
    val selectionOffset = 2.dp
    val railLabelSize = 12.sp
    val railLabelLineHeight = 14.sp
    val railDividerWidth = 24.dp
    val dashLength = 3.dp
    val badgeHeight = 20.dp
    val badgePadding = 6.dp
    val badgeRing = 2.dp
    val badgeSize = 12.sp

    /** A swipe released faster than this, per second, opens or closes the sidebar regardless of position. */
    val flingVelocity = 400.dp

    /** How far a section or folder chevron turns when it opens. */
    const val CHEVRON_OPEN_DEGREES = 90f
}
