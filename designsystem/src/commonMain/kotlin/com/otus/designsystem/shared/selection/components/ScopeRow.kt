package com.otus.designsystem.shared.selection.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_keep
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.people.components.SilentAvatarStack
import com.otus.designsystem.shared.sidebar.elements.SidebarCount
import com.otus.designsystem.shared.sidebar.elements.SidebarExpandButton
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.overlays.ScopeSheetTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A top-level [ListSheet][com.otus.designsystem.shared.overlays.components.ListSheet] choice, such
 * as every account, an account, or a pinned address. A row with a subtitle is a group, so its title
 * is SemiBold. A group with nested [ScopeChildRow]s passes [expanded] and gets a chevron that calls
 * [onExpandedChange]; the caller shows the nested rows in a
 * [Collapsible][com.otus.designsystem.shared.rows.components.Collapsible]. The [chosen] row gets a
 * `selectedFill` fill. A long press calls [onLongClick], which pins or unpins the choice.
 */
@Composable
public fun ScopeRow(
    option: ScopeOption,
    chosen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    expanded: Boolean? = null,
    onExpandedChange: (Boolean) -> Unit = {},
) {
    ScopeRowFrame(option, chosen, onClick, onLongClick, modifier, nested = false) {
        if (expanded != null) SidebarExpandButton(option.title, expanded, onExpandedChange)
    }
}

/**
 * A [ListSheet][com.otus.designsystem.shared.overlays.components.ListSheet] choice nested under a
 * [ScopeRow], such as one address of an account. It is shorter, with a smaller avatar.
 */
@Composable
public fun ScopeChildRow(
    option: ScopeOption,
    chosen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    ScopeRowFrame(option, chosen, onClick, onLongClick, modifier, nested = true) {}
}

@Composable
private fun ScopeRowFrame(
    option: ScopeOption,
    chosen: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier,
    nested: Boolean,
    trailing: @Composable RowScope.() -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = SmoothCornerShape(SheetTokens.rowRadius)
    Row(
        modifier
            .fillMaxWidth()
            .padding(start = if (nested) ScopeSheetTokens.childIndent else 0.dp)
            .heightIn(min = if (nested) ScopeSheetTokens.childHeight else ScopeSheetTokens.rowHeight)
            .semantics {
                selected = chosen
                if (option.pinned) stateDescription = if (chosen) "Selected, pinned" else "Pinned"
            }.combinedClickable(
                interactions,
                indication = null,
                role = Role.RadioButton,
                onLongClickLabel = onLongClick?.let { if (option.pinned) "Unpin" else "Pin" },
                onLongClick = onLongClick,
                onClick = onClick,
            ).clip(shape)
            .background(if (chosen) OtusColors.selectedFill else Color.Transparent)
            .controlPressFeedback(interactions, shape)
            .padding(horizontal = SheetTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap),
    ) {
        SilentAvatarStack(option.avatars, if (nested) ScopeSheetTokens.childAvatar else ScopeSheetTokens.avatar)
        ScopeText(option, Modifier.weight(1f))
        if (option.count != null) SidebarCount(option.count)
        if (option.pinned) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) { Symbol(Res.drawable.symbol_keep) }
        }
        trailing()
    }
}

@Composable
private fun ScopeText(
    option: ScopeOption,
    modifier: Modifier,
) {
    Column(modifier) {
        Text(
            option.title,
            style = panelTextStyle(),
            fontWeight = if (option.subtitle != null) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (option.subtitle != null) {
            Text(
                option.subtitle,
                style = panelTextStyle(),
                color = OtusColors.text2,
                fontSize = SheetTokens.detailSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
