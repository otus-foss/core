package com.otusfoss.core.designsystem.shared.folders.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.theme.colors.FolderColor
import com.otusfoss.core.designsystem.theme.tokens.rows.FolderTokens

/**
 * Up to [FolderTokens.STACK_LIMIT] small folder glyphs that overlap, such as the folders of
 * a conversation in a strip with little room. Each follows the [FolderGlyph] rules, so a colored
 * folder shows its tint. The caller says how many there are in text beside it.
 */
@Composable
public fun FolderStack(
    colors: List<FolderColor?>,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(-FolderTokens.glyphOverlap)) {
        colors.take(FolderTokens.STACK_LIMIT).forEach { FolderGlyph(it, size = FolderTokens.glyphSize) }
    }
}
