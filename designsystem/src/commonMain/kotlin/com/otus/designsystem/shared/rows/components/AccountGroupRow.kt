package com.otus.designsystem.shared.rows.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.shared.controls.elements.ExpandChevron
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.people.components.ChipAvatar
import com.otus.designsystem.shared.people.components.SilentAvatarStack
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.ScopeSheetTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.tokens.rows.FolderRowTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * Heads one account's part of a list, such as its folders: a small [avatar], the account's
 * [title], a [detail] such as "6 folders", an optional [action] such as a New folder button, and
 * a chevron. Tapping it folds the part away.
 */
@Composable
public fun AccountGroupRow(
    title: String,
    avatar: ChipAvatar,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val interactions = remember { MutableInteractionSource() }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = PanelTokens.touchTarget)
            .semantics { heading() }
            .toggleable(expanded, interactions, indication = null, role = Role.Button, onValueChange = onExpandedChange)
            .controlPressFeedback(interactions)
            .padding(start = SheetTokens.gap, end = FolderRowTokens.endPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap),
    ) {
        SilentAvatarStack(listOf(avatar), ScopeSheetTokens.childAvatar)
        Text(title, Modifier.weight(1f), style = panelTextStyle(), maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (detail != null) Text(detail, style = panelTextStyle(), color = OtusColors.text3, maxLines = 1)
        // The same two columns as a FolderSettingRow's settings icon and drag handle.
        TrailingIcons(first = action, last = { ExpandChevron(expanded) })
    }
}
