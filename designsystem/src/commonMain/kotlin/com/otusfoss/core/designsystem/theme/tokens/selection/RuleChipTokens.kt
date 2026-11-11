package com.otusfoss.core.designsystem.theme.tokens.selection

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.layout.CornerTokens
import com.otusfoss.core.designsystem.theme.tokens.settings.SettingTokens

/** Rule chips: a label column, an avatar or icon slot, and the value, in an outline. */
internal object RuleChipTokens {
    val height: Dp = Sizes.chipHeight
    val radius: Dp = SettingTokens.valueRadius
    val padding: Dp = SettingTokens.valuePaddingHorizontal
    val gap: Dp = 8.dp

    /** Every label column is this wide, so the slots and values of stacked chips line up. */
    val labelWidth: Dp = 52.dp

    /** How opaque a rule shown only for context is, such as a parent folder's. */
    const val MUTED_ALPHA: Float = 0.6f
    val labelSize: TextUnit = 12.sp
    val labelLineHeight: TextUnit = 16.sp
    val slot: Dp = Sizes.iconSize
    val slotIcon: Dp = 18.dp
    val valueSize: TextUnit = 15.sp
    val valueLineHeight: TextUnit = 20.sp

    /** The one-line chip under a folder's name, sized to its content without a label column. */
    val compactHeight: Dp = 24.dp
    val compactRadius: Dp = CornerTokens.small
    val compactPadding: Dp = 8.dp
    val compactGap: Dp = 6.dp
    val compactAvatar: Dp = 16.dp
    val compactValueSize: TextUnit = SettingTokens.descriptionSize
    val compactValueLineHeight: TextUnit = 18.sp

    /** The small title over an "All of" group. */
    val groupTitleTop: Dp = 8.dp
    val groupTitleSize: TextUnit = 11.sp
    val groupTitleLineHeight: TextUnit = 14.sp
    val groupTitleTracking: TextUnit = 0.8.sp
}
