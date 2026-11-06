package com.otus.designsystem.theme.tokens.controls

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Chips, button pairs, toggles, badges on icon buttons, and progress rings. */
public object ControlTokens {
    public val chipPadding: Dp = 10.dp
    public val chipDividerHeight: Dp = 14.dp
    public val chipAvatarOverlap: Dp = 6.dp
    public const val CHIP_AVATAR_LIMIT: Int = 4

    /** The primary pill's share of a button pair's width; the secondary label gets the rest. */
    public const val PAIR_PRIMARY_SHARE: Float = 0.62f

    /** A progress ring beside a label, such as while a count is still loading. */
    public val progressSize: Dp = 16.dp
    public val progressStroke: Dp = 2.dp

    /** A segmented toggle: its height and the inset of the selected segment, and a compact segment's height. */
    public val toggleHeight: Dp = 48.dp
    public val toggleInset: Dp = 4.dp
    public val segmentHeight: Dp = 40.dp

    /** A count badge on an icon button. */
    public val badgeSize: Dp = 16.dp
    public val badgeTextSize: TextUnit = 10.sp

    /** How far a section or folder chevron turns when it opens. */
    public const val CHEVRON_OPEN_DEGREES: Float = 90f
}
