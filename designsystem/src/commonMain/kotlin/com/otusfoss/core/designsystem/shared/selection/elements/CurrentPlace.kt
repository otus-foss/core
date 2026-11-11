package com.otusfoss.core.designsystem.shared.selection.elements

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.tokens.layout.SidebarTokens

/**
 * Marks the row of the place the user is in, such as the folder a message is in on the Move to
 * sheet, with the same `selectedFill` fill the sidebar gives the folder on screen. Put it before
 * any indent, so the fill spans the whole row.
 */
public fun Modifier.currentPlace(): Modifier =
    clip(SmoothCornerShape(SidebarTokens.entryRadius)).background(OtusColors.selectedFill)
