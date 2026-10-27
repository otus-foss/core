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
import androidx.compose.ui.semantics.clearAndSetSemantics
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
import com.otus.designsystem.shared.people.components.AvatarStack
import com.otus.designsystem.shared.sidebar.elements.SidebarCount
import com.otus.designsystem.shared.sidebar.elements.SidebarExpandButton
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.ScopeSheetTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A top-level [ScopeSheet] choice, such as every account, an account, or a pinned address. A
 * row with a subtitle is a group, so its title is SemiBold. A group with nested [ScopeChildRow]s
 * passes [expanded] and gets a chevron that calls [onExpandedChange]; the caller shows the
 * nested rows in a [Collapsible][com.otus.designsystem.shared.rows.components.Collapsible]. The [chosen] row gets a
 * `selectedFill` fill. A long
 * press calls [onLongClick], which pins or unpins the choice.
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
        expanded?.let { SidebarExpandButton(option.title, it, onExpandedChange) }
    }
}

/**
 * A [ScopeSheet] choice nested under a [ScopeRow], such as one address of an account. It is
 * shorter, with a smaller avatar.
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
    val shape = RoundedCornerShape(SheetTokens.rowRadius)
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
        // The title names the choice; initials would only repeat it.
        AvatarStack(
            option.avatars,
            Modifier.clearAndSetSemantics {},
            ring = OtusColors.bgLight,
            size = if (nested) ScopeSheetTokens.childAvatar else ScopeSheetTokens.avatar,
        )
        ScopeText(option, Modifier.weight(1f))
        option.count?.let { SidebarCount(it) }
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
        option.subtitle?.let {
            Text(
                it,
                style = panelTextStyle(),
                color = OtusColors.text2,
                fontSize = SheetTokens.detailSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
