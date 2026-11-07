package com.otus.designsystem.shared.folders.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.resources.symbol_folder_fill
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.FolderColor
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.rows.FolderTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * A folder's small decorative symbol, such as on a mail row or a filter chip. A [color] other than
 * null or [FolderColor.Default] shows the filled folder in its tint, because a thin outline is too
 * faint to read the color from. Otherwise it shows the outlined [icon] in text3, which also suits
 * system folders such as Sent, or the inbox.
 */
@Composable
public fun FolderGlyph(
    color: FolderColor?,
    modifier: Modifier = Modifier,
    icon: DrawableResource = Res.drawable.symbol_folder,
    size: Dp = FolderTokens.markSize,
) {
    val tint = color?.tint
    val symbol = if (tint != null) Res.drawable.symbol_folder_fill else icon
    Box(modifier) {
        CompositionLocalProvider(LocalContentColor provides (tint ?: OtusColors.text3)) {
            Symbol(symbol, size = size)
        }
    }
}
