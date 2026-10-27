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
import com.otus.designsystem.resources.symbol_inbox
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.FolderColor
import com.otus.designsystem.theme.InboxTokens
import com.otus.designsystem.theme.OtusColors
import org.jetbrains.compose.resources.DrawableResource

/**
 * A folder's small decorative symbol, such as on a mail row or a filter chip. A [color] other than
 * null or [FolderColor.Default] shows the filled folder in its tint, because a thin outline is too
 * faint to read the color from. Otherwise it shows the outlined [icon] in text3, which also suits
 * system folders such as Sent. [FolderGlyphKind.Inbox] is always the outlined inbox in text3.
 */
@Composable
public fun FolderGlyph(
    color: FolderColor?,
    modifier: Modifier = Modifier,
    kind: FolderGlyphKind = FolderGlyphKind.Folder,
    icon: DrawableResource = Res.drawable.symbol_folder,
    size: Dp = InboxTokens.markSize,
) {
    val tint = color?.tint.takeIf { kind == FolderGlyphKind.Folder }
    val symbol =
        when {
            kind == FolderGlyphKind.Inbox -> Res.drawable.symbol_inbox
            tint != null -> Res.drawable.symbol_folder_fill
            else -> icon
        }
    Box(modifier) {
        CompositionLocalProvider(LocalContentColor provides (tint ?: OtusColors.text3)) {
            Symbol(symbol, size = size)
        }
    }
}
