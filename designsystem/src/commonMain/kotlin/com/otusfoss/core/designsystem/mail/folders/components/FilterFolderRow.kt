package com.otusfoss.core.designsystem.mail.folders.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
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
import kotlin.math.max
import kotlin.math.min

/**
 * One folder of a checkable folder tree, 48 dp tall: the [glyph], the [folder]'s name after its
 * `text3` path, a chevron right after the name when it has subfolders ([expansion] is null for
 * others), and a check circle at the end. Each depth level indents [FilterTokens.folderIndent], up
 * to [FilterTokens.FOLDER_MAX_DEPTH] levels, with a guide line under each parent's
 * [ControlTokens.chipIconSize] glyph. [subrow] shows under the row at its subfolders' indent, such
 * as an option for the ticked folder, with the guide lines running on beside it.
 */
@Composable
public fun FilterFolderRow(
    folder: TreeFolder,
    check: FolderCheck,
    onCheck: () -> Unit,
    modifier: Modifier = Modifier,
    expansion: FolderExpansion? = null,
    subrow: (@Composable () -> Unit)? = null,
    glyph: @Composable () -> Unit,
) {
    val depth = folder.depth.coerceIn(0, FilterTokens.FOLDER_MAX_DEPTH - 1)
    val enabled = check != FolderCheck.Included
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(FilterTokens.folderRowHeight)
                .folderSemantics(check, folder.depth, expansion)
                .then(if (enabled) Modifier.clickable(onClick = onCheck) else Modifier)
                .folderTreeIndent(depth)
                .padding(end = FilterTokens.bodyInset),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ControlTokens.chipIconGap),
        ) {
            glyph()
            FolderName(
                ChipLabel(folder.name, folder.path).annotated(),
                expansion,
                Modifier.weight(1f),
            )
            Spacer(Modifier.width(Spacing.iconGap))
            Box(Modifier.alpha(if (enabled) 1f else OtusAlpha.DISABLED)) {
                PickMarkView(PickMark(check != FolderCheck.Off, style = PickStyle.CheckCircle))
            }
        }
        subrow?.let { content ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .folderTreeIndent((depth + 1).coerceAtMost(FilterTokens.FOLDER_MAX_DEPTH - 1)),
            ) { content() }
        }
    }
}

/** The guide lines of [depth] levels, then the start padding that puts content at that depth. */
private fun Modifier.folderTreeIndent(depth: Int): Modifier =
    treeGuides(
        levels = depth,
        indent = FilterTokens.folderIndent,
        firstCenter = FilterTokens.bodyInset + ControlTokens.chipIconSize / 2,
    ).padding(start = FilterTokens.bodyInset + FilterTokens.folderIndent * depth)

private fun folderState(
    check: FolderCheck,
    depth: Int,
    expansion: FolderExpansion?,
): String =
    listOfNotNull(
        "Level ${depth + 1}",
        when (expansion?.expanded) {
            true -> "expanded"
            false -> "collapsed"
            null -> null
        },
        "included with parent".takeIf { check == FolderCheck.Included },
    ).joinToString(", ")

/**
 * Reads the row as a checkbox, with its state and, for parents, expand and collapse actions. A
 * folder included with its parent has no click action but is not marked disabled, so its expand
 * and collapse actions still reach TalkBack.
 */
private fun Modifier.folderSemantics(
    check: FolderCheck,
    depth: Int,
    expansion: FolderExpansion?,
): Modifier =
    semantics(mergeDescendants = true) {
        role = Role.Checkbox
        toggleableState = if (check == FolderCheck.Off) ToggleableState.Off else ToggleableState.On
        stateDescription = folderState(check, depth, expansion)
        if (expansion?.expanded == false) {
            expand {
                expansion.onChange(true)
                true
            }
        }
        if (expansion?.expanded == true) {
            collapse {
                expansion.onChange(false)
                true
            }
        }
    }

/**
 * The folder's [label], then for parents a chevron flush after it. The name gives up only the
 * chevron's width. A 48 dp tap area for the chevron starts at the name's end, or covers the name's
 * tail when the name fills the row, so it never reaches the check circle. The chevron's turn is the
 * tap's feedback, so it draws no ripple and adds no focus stop; the row carries the actions.
 */
@Composable
private fun FolderName(
    label: AnnotatedString,
    expansion: FolderExpansion?,
    modifier: Modifier,
) {
    val name = @Composable {
        Text(label, style = panelTextStyle(), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    if (expansion == null) {
        Box(modifier) { name() }
        return
    }
    val latest by rememberUpdatedState(expansion)
    Layout(
        listOf(
            name,
            { ExpandChevron(expansion.expanded, size = FilterTokens.folderChevronSize) },
            {
                Box(
                    Modifier.pointerInput(Unit) {
                        detectTapGestures { latest.onChange(!latest.expanded) }
                    },
                )
            },
        ),
        modifier,
    ) { (nameParts, chevronParts, tapParts), constraints ->
        val gap = Spacing.iconGap.roundToPx()
        val chevron = chevronParts.single().measure(Constraints())
        val text =
            nameParts.single().measure(
                Constraints(maxWidth = (constraints.maxWidth - gap - chevron.width).coerceAtLeast(0)),
            )
        val height =
            if (constraints.hasBoundedHeight) constraints.maxHeight else max(text.height, chevron.height)
        val tapWidth = min(Sizes.touchTarget.roundToPx(), constraints.maxWidth)
        val tap = tapParts.single().measure(Constraints.fixed(tapWidth, height))
        layout(constraints.maxWidth, height) {
            text.placeRelative(0, (height - text.height) / 2)
            chevron.placeRelative(text.width + gap, (height - chevron.height) / 2)
            tap.placeRelative(min(text.width, constraints.maxWidth - tapWidth), 0)
        }
    }
}
