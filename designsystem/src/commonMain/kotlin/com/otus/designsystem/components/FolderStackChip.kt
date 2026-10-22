package com.otus.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.otus.designsystem.elements.Chip
import com.otus.designsystem.elements.ChipLabel
import com.otus.designsystem.elements.FolderGlyph
import com.otus.designsystem.theme.FilterTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

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
    Chip(null, onClick, modifier.semantics { contentDescription = description }, selected = true) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PanelTokens.iconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(-FilterTokens.folderStackOverlap)) {
                folders.take(FilterTokens.FOLDER_STACK_LIMIT).forEach {
                    FolderGlyph(it.color, icon = it.icon, size = PanelTokens.iconSize)
                }
            }
            val faint = OtusColors.text3
            val text =
                buildAnnotatedString {
                    label.prefix?.let { withStyle(SpanStyle(color = faint)) { append("$it / ") } }
                    append(label.text)
                }
            Text(text, style = panelTextStyle().copy(color = Color.Unspecified), maxLines = 1)
            more?.let { Text(it, style = panelTextStyle(), color = OtusColors.text2, fontSize = PanelTokens.countSize) }
        }
    }
}
