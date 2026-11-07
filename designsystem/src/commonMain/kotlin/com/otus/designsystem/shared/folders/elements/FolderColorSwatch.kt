package com.otus.designsystem.shared.folders.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.otus.designsystem.theme.colors.FolderColor
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.settings.ThemeTokens

/**
 * A small circle filled with [color]'s tint in the current theme, or an empty `text2` ring for
 * [FolderColor.Default]. It is decoration: the option beside it names the color.
 */
@Composable
public fun FolderColorSwatch(
    color: FolderColor,
    modifier: Modifier = Modifier,
) {
    val tint = color.tint
    Box(
        modifier
            .size(ThemeTokens.folderSwatch)
            .folderSwatchFill(tint),
    )
}

private fun Modifier.folderSwatchFill(tint: Color?): Modifier =
    if (tint == null) border(Sizes.borderWidth, OtusColors.text2, CircleShape) else background(tint, CircleShape)
