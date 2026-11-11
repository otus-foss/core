package com.otusfoss.core.designsystem.mail.folders.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_folder
import com.otusfoss.core.designsystem.resources.symbol_settings
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.controls.elements.controlPressFeedback
import com.otusfoss.core.designsystem.shared.folders.elements.FolderToggle
import com.otusfoss.core.designsystem.shared.overlays.components.onPressDown
import com.otusfoss.core.designsystem.shared.people.components.AvatarStack
import com.otusfoss.core.designsystem.shared.people.components.ChipAvatar
import com.otusfoss.core.designsystem.shared.reorder.components.ReorderableScope
import com.otusfoss.core.designsystem.shared.rows.components.RowDensity
import com.otusfoss.core.designsystem.shared.rows.components.TrailingIcons
import com.otusfoss.core.designsystem.shared.rows.elements.MAX_INDENT_STEPS
import com.otusfoss.core.designsystem.shared.rows.elements.nestIndent
import com.otusfoss.core.designsystem.shared.sidebar.elements.SidebarCount
import com.otusfoss.core.designsystem.shared.sidebar.elements.SidebarFolder
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens
import com.otusfoss.core.designsystem.theme.tokens.rows.FolderRowTokens
import com.otusfoss.core.designsystem.theme.tokens.selection.RuleChipTokens
import com.otusfoss.core.designsystem.theme.tokens.settings.SettingTokens

/**
 * A folder on a folder settings page, indented by its [SidebarFolder.depth]: the folder icon, the
 * name with an optional [detail] line under it, the optional count, and a settings icon. Tapping
 * the row calls [onClick], which opens the folder; a long press calls [onLongClick] with the
 * pressed point, such as to open a [QuickActionBar][com.otusfoss.core.designsystem.shared.actionbar.components.QuickActionBar]
 * there. With subfolders, [expanded] is non-null and the icon is a toggle, an open folder while
 * expanded. [SidebarFolder.icon] replaces the closed folder icon, such as for a shared folder. The
 * icon takes [com.otusfoss.core.designsystem.shared.controls.elements.LocalIconTint].
 */
@Composable
public fun FolderSettingRow(
    folder: SidebarFolder,
    expanded: Boolean?,
    onExpandedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: ((Offset) -> Unit)? = null,
    detail: (@Composable () -> Unit)? = null,
) {
    FolderRowLayout(folder, expanded, onExpandedChange, modifier, FolderRowSlots(onClick, onLongClick, detail))
}

/**
 * A [FolderSettingRow] in a [ReorderableTree][com.otusfoss.core.designsystem.shared.reorder.components.ReorderableTree]
 * or [ReorderableColumn][com.otusfoss.core.designsystem.shared.reorder.components.ReorderableColumn], with
 * the row's drag handle after its settings icon.
 */
@Composable
public fun ReorderableScope.FolderSettingRow(
    folder: SidebarFolder,
    expanded: Boolean?,
    onExpandedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: ((Offset) -> Unit)? = null,
    detail: (@Composable () -> Unit)? = null,
) {
    FolderRowLayout(
        folder,
        expanded,
        onExpandedChange,
        modifier,
        FolderRowSlots(onClick, onLongClick, detail) { DragHandle(folder.label) },
    )
}

/** What a folder row does when pressed, and the parts a caller adds to it. */
private class FolderRowSlots(
    val onClick: () -> Unit,
    val onLongClick: ((Offset) -> Unit)?,
    val detail: (@Composable () -> Unit)?,
    val handle: (@Composable () -> Unit)? = null,
)

@Composable
private fun FolderRowLayout(
    folder: SidebarFolder,
    expanded: Boolean?,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier,
    slots: FolderRowSlots,
) {
    val interactions = remember { MutableInteractionSource() }
    var pressedAt by remember { mutableStateOf(Offset.Zero) }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = FolderRowTokens.minHeight)
            .onPressDown { pressedAt = it }
            .combinedClickable(
                interactions,
                indication = null,
                role = Role.Button,
                onLongClick = slots.onLongClick?.let { { it(pressedAt) } },
                onClick = slots.onClick,
            ).controlPressFeedback(interactions, SmoothCornerShape(SheetTokens.rowRadius))
            .treeGuides(folder.depth)
            .nestIndent(FolderRowTokens.nestIndent, folder.depth)
            .padding(
                end = FolderRowTokens.endPadding,
                top = FolderRowTokens.verticalPadding,
                bottom = FolderRowTokens.verticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(FolderRowTokens.nameGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FolderToggle(folder.label, expanded, onExpandedChange, folder.icon ?: Res.drawable.symbol_folder)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(FolderRowTokens.lineGap)) {
            Text(
                folder.label,
                fontSize = SettingTokens.titleSize,
                lineHeight = SettingTokens.titleLineHeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            slots.detail?.invoke()
        }
        val count = folder.count
        if (count != null) SidebarCount(count)
        TrailingIcons(
            first = {
                CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                    Symbol(Res.drawable.symbol_settings)
                }
            },
            last = slots.handle,
        )
    }
}

/**
 * A [FolderSettingRow] detail: the folder's first rule as a compact [RuleChip], then [more] in
 * `text3`, such as "+1" for the rules it does not show.
 */
@Composable
public fun FolderRuleSummary(
    line: RuleChipLine,
    more: String? = null,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(RuleChipTokens.compactGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RuleChip(line, Modifier.weight(1f, fill = false), RowDensity.Compact)
        if (more != null) {
            Text(
                more,
                color = OtusColors.text3,
                fontSize = RuleChipTokens.labelSize,
                lineHeight = RuleChipTokens.labelLineHeight,
                maxLines = 1,
            )
        }
    }
}

/**
 * A [FolderSettingRow] detail for a folder someone else owns: [owner]'s small avatar before
 * [text], such as "Shared by Florian · can read", in `text2`.
 */
@Composable
public fun FolderOwnerLine(
    owner: ChipAvatar,
    text: String,
) {
    AvatarStack(listOf(owner), label = text, ring = OtusColors.bgLight, size = RuleChipTokens.compactAvatar)
}

/**
 * A thin line down the row under each folder icon above it, one per level of [depth], so nested
 * rows read as a tree. Rows follow each other without gaps, so the lines join up.
 */
private fun Modifier.treeGuides(depth: Int): Modifier =
    drawBehind {
        val width = Sizes.borderWidth.toPx()
        repeat(depth.coerceAtMost(MAX_INDENT_STEPS)) { level ->
            val center = (FolderRowTokens.nestIndent * level + Sizes.touchTarget / 2).toPx()
            val x = if (layoutDirection == LayoutDirection.Rtl) size.width - center else center
            drawLine(OtusColors.bgLight, Offset(x, 0f), Offset(x, size.height), width)
        }
    }
