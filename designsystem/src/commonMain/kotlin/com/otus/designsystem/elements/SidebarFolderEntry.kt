package com.otus.designsystem.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SidebarTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A folder in the sidebar tree. With subfolders, [expanded] is non-null and the icon is a toggle:
 * an open folder while expanded, a closed one while collapsed. Tapping the row calls [onClick];
 * a long press calls [onLongClick], such as to open the folder's actions. The icon takes
 * [LocalIconTint], such as the folder's color, and is a filled folder then; otherwise it is an
 * outlined folder in text3. It fits [com.otus.designsystem.components.CollapsibleTree]'s
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
            .nestIndent(SidebarTokens.nestIndent, folder.depth)
            .padding(end = SidebarTokens.entryPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Untinted folders step back in text3, so colored ones stand out.
        CompositionLocalProvider(LocalContentColor provides OtusColors.text3) {
            FolderToggle(folder.label, expanded, onExpandedChange)
        }
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
