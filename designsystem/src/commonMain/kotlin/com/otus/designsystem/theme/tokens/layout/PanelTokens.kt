package com.otus.designsystem.theme.tokens.layout

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.core.Spacing
import com.otus.designsystem.theme.typography.TypeScale

public object PanelTokens {
    public val navigationIconSize: Dp = 32.dp
    public val closeIconSize: Dp = 24.dp
    public val panelInset: Dp = 8.dp
    public val headerHeight: Dp = Sizes.touchTarget + panelInset * 2
    public val panelRadius: Dp = CornerTokens.panel

    /** The control panel's one header row, below the status bar. */
    public val headerRowHeight: Dp = 56.dp

    /** Space above and below the header's 48 dp content, which centers one line in [headerRowHeight]. */
    public val headerContentPadding: Dp = (headerRowHeight - Sizes.touchTarget) / 2

    /** The control panel runs edge to edge from the top, so only its bottom corners round. */
    public val bottomRadius: Dp = CornerTokens.surface
    public val bottomShape: Shape =
        SmoothCornerShape(CornerSize(0.dp), CornerSize(0.dp), CornerSize(bottomRadius), CornerSize(bottomRadius))

    /**
     * A panel sheet, the panel unfolded for a task: how far its contents slide while they fade in,
     * the upward swipe that closes it, and the grab handle at its bottom.
     */
    public val sheetContentSlide: Dp = 8.dp
    public val sheetCloseSwipe: Dp = 72.dp

    /** An upward flick faster than this, per second, closes a panel sheet the user has started to lift. */
    public val sheetCloseFling: Dp = 800.dp
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
    public val headerInset: Dp = Spacing.gutter

    /** The gap between header content and the account stack. */
    public val headerGap: Dp = 6.dp

    /** How far below the header row the panel's edge, and the pills across it, sit at the top of a list. */
    public val railDrop: Dp = 30.dp

    /** The panel's edge below the header row once collapsed, or on a page without pills. */
    public val collapsedDrop: Dp = 4.dp

    /** How far the list scrolls to collapse the panel completely. */
    public val collapseDistance: Dp = 60.dp

    /** How far the pills rise into the panel, and how much they shrink, as they fade on collapse. */
    public val pillRise: Dp = 18.dp
    public const val PILL_SHRINK: Float = 0.04f

    /** The inbox's search pill, the same height whether the panel is expanded or collapsed. */
    public val searchPillHeight: Dp = 44.dp

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
    public val badgeTextSize: TextUnit = TypeScale.badge
    public const val BADGE_START_SCALE: Float = 0.6f

    /** A page title beside the chevron. */
    public val titleSize: TextUnit = TypeScale.title
    public val titleLineHeight: TextUnit = TypeScale.titleLineHeight

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
}
