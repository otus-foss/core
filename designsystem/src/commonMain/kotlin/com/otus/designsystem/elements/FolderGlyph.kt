package com.otus.designsystem.elements

import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.resources.symbol_folder_fill
import com.otus.designsystem.theme.FilterTokens
import com.otus.designsystem.theme.FolderColor
import com.otus.designsystem.theme.OtusColors
import org.jetbrains.compose.resources.DrawableResource

/**
 * A folder's symbol where folders are picked, such as filter chips: a filled folder in the
 * folder's [color], or the outlined [icon] in `text3` for [FolderColor.Default], which also suits
 * system folders such as Sent.
 */
@Composable
public fun FolderGlyph(
    color: FolderColor,
    icon: DrawableResource = Res.drawable.symbol_folder,
    size: Dp = FilterTokens.chipIconSize,
) {
    val tint = color.tint
    CompositionLocalProvider(LocalContentColor provides (tint ?: OtusColors.text3)) {
        Symbol(if (tint == null) icon else Res.drawable.symbol_folder_fill, size = size)
    }
}
