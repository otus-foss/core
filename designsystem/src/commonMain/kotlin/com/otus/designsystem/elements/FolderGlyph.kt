package com.otus.designsystem.elements

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
import com.otus.designsystem.theme.FolderColor
import com.otus.designsystem.theme.InboxTokens
import com.otus.designsystem.theme.OtusColors

/**
 * A small decorative icon for the folder an item is in. [color] null or [FolderColor.Default]
 * shows the outlined glyph in text3. Any other color shows the filled glyph in its tint, because a
 * thin outline is too faint to read the color from. [FolderGlyphKind.Inbox] is always the outlined
 * inbox in text3.
 */
@Composable
public fun FolderGlyph(
    kind: FolderGlyphKind,
    color: FolderColor?,
    modifier: Modifier = Modifier,
    size: Dp = InboxTokens.markSize,
) {
    val tint = color?.tint.takeIf { kind == FolderGlyphKind.Folder }
    val symbol =
        when {
            kind == FolderGlyphKind.Inbox -> Res.drawable.symbol_inbox
            tint != null -> Res.drawable.symbol_folder_fill
            else -> Res.drawable.symbol_folder
        }
    Box(modifier) {
        CompositionLocalProvider(LocalContentColor provides (tint ?: OtusColors.text3)) {
            Symbol(symbol, size = size)
        }
    }
}
