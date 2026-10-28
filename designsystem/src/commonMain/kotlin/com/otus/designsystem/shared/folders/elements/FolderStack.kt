package com.otus.designsystem.shared.folders.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.theme.colors.FolderColor
import com.otus.designsystem.theme.tokens.content.ReadTokens

/**
 * Up to [ReadTokens.FOLDER_STACK_LIMIT] small folder glyphs that overlap, such as the folders of
 * a conversation in a strip with little room. Each follows the [FolderGlyph] rules, so a colored
 * folder shows its tint. The caller says how many there are in text beside it.
 */
@Composable
public fun FolderStack(
    colors: List<FolderColor?>,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(-ReadTokens.folderOverlap)) {
        colors.take(ReadTokens.FOLDER_STACK_LIMIT).forEach { FolderGlyph(it, size = ReadTokens.folderGlyphSize) }
    }
}
