package com.otus.designsystem.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.resources.symbol_folder_open
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SidebarTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A folder in the sidebar tree. With subfolders, [expanded] is non-null and the icon is a toggle:
 * an open folder while expanded, a closed one while collapsed. Tapping the row calls [onClick];
 * a long press calls [onLongClick], such as to open the folder's actions. The icon takes
 * [LocalIconTint], such as the folder's color. It fits [com.otus.designsystem.components.CollapsibleTree]'s
 * row callback.
 */
@Composable
public fun SidebarFolderEntry(
    folder: SidebarFolder,
    selected: Boolean,
    expanded: Boolean?,
    onExpandedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(SidebarTokens.entryRadius)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = PanelTokens.compactRowHeight)
            .semantics { this.selected = selected }
            .combinedClickable(
                interactions,
                indication = null,
                role = Role.Tab,
                onLongClick = onLongClick,
                onClick = onClick,
            ).clip(shape)
            .background(if (selected) OtusColors.accentDark else Color.Transparent)
            .controlPressFeedback(interactions, shape)
            .padding(start = SidebarTokens.nestIndent * folder.depth, end = SidebarTokens.entryPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FolderToggle(folder.label, expanded, onExpandedChange)
        Text(
            folder.label,
            Modifier.weight(1f),
            style = panelTextStyle().copy(color = Color.Unspecified),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        folder.count?.let { SidebarCount(it) }
    }
}

/** The folder icon in a target 48 dp wide and one row tall; it folds the subfolders when there are any. */
@Composable
private fun FolderToggle(
    label: String,
    expanded: Boolean?,
    onExpandedChange: (Boolean) -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    val toggle =
        expanded?.let {
            Modifier
                .semantics { contentDescription = if (it) "Collapse $label" else "Expand $label" }
                .toggleable(it, interactions, indication = null, role = Role.Button, onValueChange = onExpandedChange)
        } ?: Modifier
    Box(
        Modifier.size(PanelTokens.touchTarget, PanelTokens.compactRowHeight).then(toggle),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(SidebarTokens.expandButton).controlPressFeedback(interactions),
            contentAlignment = Alignment.Center,
        ) { LeadingSymbol(if (expanded == true) Res.drawable.symbol_folder_open else Res.drawable.symbol_folder) }
    }
}
