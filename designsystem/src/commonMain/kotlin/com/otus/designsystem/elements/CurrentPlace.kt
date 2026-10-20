package com.otus.designsystem.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SidebarTokens

/**
 * Marks the row of the place the user is in, such as the folder a message is in on the Move to
 * sheet, with the same `accentDark` fill the sidebar gives the folder on screen. Put it before
 * any indent, so the fill spans the whole row.
 */
public fun Modifier.currentPlace(): Modifier =
    clip(RoundedCornerShape(SidebarTokens.entryRadius)).background(OtusColors.accentDark)
