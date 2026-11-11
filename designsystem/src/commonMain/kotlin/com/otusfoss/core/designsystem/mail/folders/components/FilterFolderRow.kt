package com.otusfoss.core.designsystem.mail.folders.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import com.otusfoss.core.designsystem.shared.controls.elements.ChipLabel
import com.otusfoss.core.designsystem.shared.controls.elements.ExpandChevron
import com.otusfoss.core.designsystem.shared.controls.elements.annotated
import com.otusfoss.core.designsystem.shared.selection.components.PickMark
import com.otusfoss.core.designsystem.shared.selection.components.PickMarkView
import com.otusfoss.core.designsystem.shared.selection.components.PickStyle
import com.otusfoss.core.designsystem.theme.colors.OtusAlpha
import com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.tokens.selection.FilterTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * One folder of a checkable folder tree, 48 dp tall: a chevron for folders with subfolders
 * ([expanded] is null for others), the [glyph], the [folder]'s name after its `text3` path, and a
 * check circle at the end. Each depth level indents [FilterTokens.folderIndent], up to
 * [FilterTokens.FOLDER_MAX_DEPTH] levels.
 */
@Composable
public fun FilterFolderRow(
    folder: TreeFolder,
    check: FolderCheck,
    onCheck: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean? = null,
    onExpandedChange: (Boolean) -> Unit = {},
    glyph: @Composable () -> Unit,
) {
    val depth = folder.depth
    val enabled = check != FolderCheck.Included
    Row(
        modifier
            .fillMaxWidth()
            .height(FilterTokens.folderRowHeight)
            .folderSemantics(check, depth, expanded, onExpandedChange)
            .clickable(enabled = enabled, onClick = onCheck)
            .padding(
                start = FilterTokens.folderIndent * depth.coerceIn(0, FilterTokens.FOLDER_MAX_DEPTH - 1),
                end = FilterTokens.bodyInset,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ControlTokens.chipIconGap),
    ) {
        FolderExpandButton(expanded, onExpandedChange)
        glyph()
        Text(
            ChipLabel(folder.name, folder.path).annotated(),
            Modifier.weight(1f),
            style = panelTextStyle(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(Spacing.iconGap))
        Box(Modifier.alpha(if (enabled) 1f else OtusAlpha.DISABLED)) {
            PickMarkView(PickMark(check != FolderCheck.Off, style = PickStyle.CheckCircle))
        }
    }
}

private fun folderState(
    check: FolderCheck,
    depth: Int,
    expanded: Boolean?,
): String =
    listOfNotNull(
        "Level ${depth + 1}",
        when (expanded) {
            true -> "expanded"
            false -> "collapsed"
            null -> null
        },
        "included with parent".takeIf { check == FolderCheck.Included },
    ).joinToString(", ")

/** Reads the row as a checkbox, with its state and, for parents, expand and collapse actions. */
private fun Modifier.folderSemantics(
    check: FolderCheck,
    depth: Int,
    expanded: Boolean?,
    onExpandedChange: (Boolean) -> Unit,
): Modifier =
    semantics(mergeDescendants = true) {
        role = Role.Checkbox
        toggleableState = if (check == FolderCheck.Off) ToggleableState.Off else ToggleableState.On
        stateDescription = folderState(check, depth, expanded)
        if (check == FolderCheck.Included) disabled()
        if (expanded == false) {
            expand {
                onExpandedChange(true)
                true
            }
        }
        if (expanded == true) {
            collapse {
                onExpandedChange(false)
                true
            }
        }
    }

@Composable
private fun FolderExpandButton(
    expanded: Boolean?,
    onExpandedChange: (Boolean) -> Unit,
) {
    Box(Modifier.size(Sizes.touchTarget), contentAlignment = Alignment.Center) {
        if (expanded != null) {
            Box(
                Modifier
                    .size(Sizes.touchTarget)
                    .clearAndSetSemantics {}
                    .clickable { onExpandedChange(!expanded) },
                contentAlignment = Alignment.Center,
            ) { ExpandChevron(expanded) }
        }
    }
}
