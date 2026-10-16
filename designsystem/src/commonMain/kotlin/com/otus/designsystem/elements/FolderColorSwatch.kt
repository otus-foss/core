package com.otus.designsystem.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.theme.FolderColor
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.ThemeTokens

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
            .then(
                if (tint == null) {
                    Modifier.border(PanelTokens.borderWidth, OtusColors.text2, CircleShape)
                } else {
                    Modifier.background(tint, CircleShape)
                },
            ),
    )
}
