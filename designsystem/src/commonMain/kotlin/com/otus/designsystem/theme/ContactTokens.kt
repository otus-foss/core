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
}
