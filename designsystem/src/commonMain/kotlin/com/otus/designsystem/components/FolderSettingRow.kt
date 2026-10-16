package com.otus.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.FolderToggle
import com.otus.designsystem.elements.SidebarCount
import com.otus.designsystem.elements.SidebarFolder
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.elements.nestIndent
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_right
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.theme.FolderRowTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.RuleChipTokens
import com.otus.designsystem.theme.SettingTokens
import com.otus.designsystem.theme.SheetTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * A folder on a folder settings page, indented by its [SidebarFolder.depth]: the folder icon, the
 * name with an optional [detail] line under it, the optional count, and a chevron. Tapping the row
 * calls [onClick], which opens the folder. With subfolders, [expanded] is non-null and the icon is
 * a toggle, an open folder while expanded. [icon] replaces the closed folder icon, such as for a
 * shared folder. The icon takes [com.otus.designsystem.elements.LocalIconTint]. Inside a
 * [ReorderableColumn], pass [ReorderableScope.dragOnLongPress] in [modifier] so a long press drags it.
 */
@Composable
public fun FolderSettingRow(
    folder: SidebarFolder,
    expanded: Boolean?,
    onExpandedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: DrawableResource? = null,
    detail: (@Composable () -> Unit)? = null,
) {
    val interactions = remember { MutableInteractionSource() }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = FolderRowTokens.minHeight)
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
            .controlPressFeedback(interactions, RoundedCornerShape(SheetTokens.rowRadius))
            .nestIndent(FolderRowTokens.nestIndent, folder.depth)
            .padding(
                end = FolderRowTokens.endPadding,
                top = FolderRowTokens.verticalPadding,
                bottom = FolderRowTokens.verticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(FolderRowTokens.nameGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FolderToggle(folder.label, expanded, onExpandedChange, icon ?: Res.drawable.symbol_folder)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(FolderRowTokens.lineGap)) {
            Text(
                folder.label,
                fontSize = SettingTokens.titleSize,
                lineHeight = SettingTokens.titleLineHeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            detail?.invoke()
        }
        folder.count?.let { SidebarCount(it) }
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
            Symbol(Res.drawable.symbol_chevron_right, mirrorInRtl = true)
        }
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
        more?.let {
            Text(
                it,
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
