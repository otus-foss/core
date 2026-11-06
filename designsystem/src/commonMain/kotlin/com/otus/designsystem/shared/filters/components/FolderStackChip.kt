package com.otus.designsystem.shared.filters.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.shared.controls.elements.Chip
import com.otus.designsystem.shared.filters.elements.ChipLabel
import com.otus.designsystem.shared.filters.elements.annotated
import com.otus.designsystem.shared.folders.elements.FolderGlyph
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.core.Spacing
import com.otus.designsystem.theme.tokens.selection.FilterTokens
import com.otus.designsystem.theme.typography.TypeScale
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A selected pill for chosen folders: up to [FilterTokens.FOLDER_STACK_LIMIT] overlapping
 * [folders], the first folder's [label] with its `text3` prefix, such as "Freelancing / Invoices",
 * and an optional `text2` [more] count, such as "+2". Screen readers hear [description].
 */
@Composable
public fun FolderStackChip(
    folders: List<StackedFolder>,
    label: ChipLabel,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    more: String? = null,
) {
    Chip(label = null, onClick, modifier.semantics { contentDescription = description }, selected = true) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.iconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(-FilterTokens.folderStackOverlap)) {
                folders.take(FilterTokens.FOLDER_STACK_LIMIT).forEach {
                    FolderGlyph(it.color, icon = it.icon, size = Sizes.iconSize)
                }
            }
            Text(label.annotated(), style = panelTextStyle().copy(color = Color.Unspecified), maxLines = 1)
            if (more != null) {
                Text(more, style = panelTextStyle(), color = OtusColors.text2, fontSize = TypeScale.label)
            }
        }
    }
}
