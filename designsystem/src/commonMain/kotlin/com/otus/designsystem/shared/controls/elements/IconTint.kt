package com.otus.designsystem.shared.controls.elements

import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.resources.symbol_folder_fill
import com.otus.designsystem.theme.tokens.core.Sizes
import org.jetbrains.compose.resources.DrawableResource

/**
 * Colors the leading icon of the rows inside it, such as a folder's
 * [com.otus.designsystem.theme.colors.FolderColor.tint]. Null keeps each row's own icon color. Text,
 * backgrounds, the selected state, trailing icons, and disabled icons keep their colors. Rows
 * that draw a [LeadingSymbol] read it.
 */
public val LocalIconTint: ProvidableCompositionLocal<Color?> = compositionLocalOf { null }

/**
 * A row's leading [Symbol], in [LocalIconTint] while the row is [enabled]. A tinted plain folder
 * is filled, because a thin outline is too faint to read the color from.
 */
@Composable
internal fun LeadingSymbol(
    resource: DrawableResource,
    size: Dp = Sizes.iconSize,
    enabled: Boolean = true,
) {
    val tint = LocalIconTint.current.takeIf { enabled }
    val filled = tint != null && resource == Res.drawable.symbol_folder
    val shown = if (filled) Res.drawable.symbol_folder_fill else resource
    CompositionLocalProvider(LocalContentColor provides (tint ?: LocalContentColor.current)) {
        Symbol(shown, size = size)
    }
}
