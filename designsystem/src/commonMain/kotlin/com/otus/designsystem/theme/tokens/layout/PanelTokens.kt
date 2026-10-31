package com.otus.designsystem.theme.tokens.layout

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

public object PanelTokens {
    public val touchTarget: Dp = 48.dp

    /**
     * One row of a folder list: in the sidebar, the move sheet, and settings. Rows follow each
     * other without gaps, so they are shorter than [touchTarget] to fit more folders on screen.
     */
    public val compactRowHeight: Dp = 36.dp
    public val chipHeight: Dp = 36.dp
    public val iconSize: Dp = 20.dp
    public val chipAvatarOverlap: Dp = 6.dp
    public const val CHIP_AVATAR_LIMIT: Int = 4
    public val navigationIconSize: Dp = 32.dp
    public val closeIconSize: Dp = 24.dp
    public val chipPadding: Dp = 10.dp
    public val chipDividerHeight: Dp = 14.dp
    public val iconGap: Dp = 4.dp
    public val chipGap: Dp = 6.dp
    public val panelInset: Dp = 8.dp
    public val headerHeight: Dp = touchTarget + panelInset * 2
    public val panelRadius: Dp = CornerTokens.panel
    public val detachedGap: Dp = 0.dp

    /** The control panel's one header row, below the status bar. */
    public val headerRowHeight: Dp = 56.dp

    /** Space above and below the header's 48 dp content, which centers one line in [headerRowHeight]. */
    public val headerContentPadding: Dp = (headerRowHeight - touchTarget) / 2

    /** The control panel runs edge to edge from the top, so only its bottom corners round. */
    public val bottomRadius: Dp = CornerTokens.surface
    public val bottomShape: Shape = RoundedCornerShape(bottomStart = bottomRadius, bottomEnd = bottomRadius)

    /**
     * A panel sheet, the panel unfolded for a task: how far its contents slide while they fade in,
     * the upward swipe that closes it, and the grab handle at its bottom.
     */
    public val sheetContentSlide: Dp = 8.dp
    public val sheetCloseSwipe: Dp = 72.dp
    public val handleWidth: Dp = 36.dp
    public val handleHeight: Dp = 4.dp
    public val handlePadding: Dp = 12.dp

    /**
     * Where the header's 48 dp chevron target starts. The glyph sits at the target's start, so its
     * stroke lands about 16 dp from the screen edge.
     */
    public val navigationStart: Dp = 4.dp

    /** Where header content starts beside the chevron. The chevron's target overlaps it. */
    public val navigationContentStart: Dp = 40.dp

    /** The header's inset from a screen edge that has no chevron. */
    public val headerInset: Dp = 16.dp

    /** The gap between header content and the account stack. */
    public val headerGap: Dp = 6.dp

    /** How far below the header row the panel's edge, and the pills across it, sit at the top of a list. */
    public val railDrop: Dp = 34.dp

    /** The panel's edge below the header row once collapsed, or on a page without pills. */
    public val collapsedDrop: Dp = 4.dp

    /** How far the list scrolls to collapse the panel completely. */
    public val collapseDistance: Dp = 60.dp

    /** How far the pills rise into the panel, and how much they shrink, as they fade on collapse. */
    public val pillRise: Dp = 18.dp
    public const val PILL_SHRINK: Float = 0.04f

    /** The inbox's search pill, at the top of the list and collapsed. */
    public val searchPillHeight: Dp = 52.dp
    public val compactSearchPillHeight: Dp = 44.dp

    /** Insets inside the search pill, the gap after its icon, and its filter button's size as it starts growing in. */
    public val searchPillStart: Dp = 16.dp
    public val searchPillEnd: Dp = 6.dp
    public val searchIconGap: Dp = 10.dp
    public const val FILTER_START_SCALE: Float = 0.7f

    /** The header's account stack: avatar size, how far each covers the one before, and its ring. */
    public val stackAvatarSize: Dp = 34.dp
    public val stackAvatarOverlap: Dp = 12.dp
    public val stackRing: Dp = 2.dp

    /** Room after the stack, so its last avatar ends [headerInset] from the screen edge. */
    public val stackEnd: Dp = headerInset - panelInset

    /** The "+N" badge on a folded stack: its offset past the avatar, padding, text, and starting scale. */
    public val badgeOffset: Dp = 8.dp
    public val badgePadding: Dp = 4.dp
    public val badgeTextSize: TextUnit = 11.sp
    public const val BADGE_START_SCALE: Float = 0.6f

    /** A page title beside the chevron. */
    public val titleSize: TextUnit = 20.sp
    public val titleLineHeight: TextUnit = 26.sp

    /** The title's start after the chevron, and how far it slides away as a search field grows over it. */
    public val titleStart: Dp = 4.dp
    public val titleSlide: Dp = 16.dp

    /**
     * A subpage's search field: where it ends up, just after the chevron; how far its icon moves
     * into it as it grows; and how far the text starts before the icon's touch target ends.
     */
    public val searchFieldStart: Dp = titleStart
    public val searchIconShift: Dp = 4.dp
    public val searchInputPull: Dp = 2.dp

    /** A message a search found, in the panel's search surface: its height and the gap after its icon. */
    public val searchHitHeight: Dp = 48.dp
    public val searchHitGap: Dp = 16.dp

    /** How much a page dims behind an open search field before anything is typed. */
    public const val SEARCH_DIM: Float = 0.45f

    /** The page-colored ring that cuts each pill out of the panel. */
    public val pillRing: Dp = 4.dp
    public val cutoutGap: Dp = 2.dp
    public val borderWidth: Dp = 1.dp

    /** The primary pill's share of a button pair's width; the secondary label gets the rest. */
    public const val PAIR_PRIMARY_SHARE: Float = 0.62f

    /** A progress ring beside a label, such as while a count is still loading. */
    public val progressSize: Dp = 16.dp
    public val progressStroke: Dp = 2.dp
    public val fadeWidth: Dp = 24.dp
    public val textSize: TextUnit = 16.sp
    public val lineHeight: TextUnit = 20.sp

    /** A count beside a label, such as unread mail in a chip. */
    public val countSize: TextUnit = 14.sp
}
