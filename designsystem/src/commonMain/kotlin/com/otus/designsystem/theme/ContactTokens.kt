package com.otus.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Sizes of the contact list, a contact's page and its editor. */
internal object ContactTokens {
    val rowHeight: Dp = 60.dp
    val avatarSize: Dp = 40.dp
    val detailSize: TextUnit = 13.sp
    val headerAvatarSize: Dp = 160.dp
    val headerGap: Dp = 6.dp
    val photoBadgeSize: Dp = 32.dp
    val photoBadgeIconSize: Dp = 18.dp
    val valueRowHeight: Dp = 60.dp

    // The same height as an InputField, so every field of the contact editor lines up.
    val fieldHeight: Dp = PanelTokens.touchTarget
    val labelChipHeight: Dp = 28.dp

    /** The space an icon button leaves around its icon on each side, inside its 48 dp target. */
    private val iconRoom: Dp = (PanelTokens.touchTarget - PanelTokens.iconSize) / 2

    /** How far a field's star and ✕ touch targets overlap, so their icons sit [PageTokens.rowGap] apart. */
    val trailingOverlap: Dp = iconRoom * 2 - PageTokens.rowGap

    /** How far those targets reach past the label chip, so the star sits [PageTokens.rowGap] after it. */
    val trailingBleedStart: Dp = iconRoom + PanelTokens.iconGap - PageTokens.rowGap

    /** How far they reach past the field's end padding, so the ✕ sits [SheetTokens.inset] from the edge. */
    val trailingBleedEnd: Dp = iconRoom + PanelTokens.iconGap - SheetTokens.inset
}
