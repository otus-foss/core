package com.otus.designsystem.shared.sidebar.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.shared.controls.elements.ExpandChevron
import com.otus.designsystem.shared.controls.elements.LeadingSymbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.rows.elements.nestIndent
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.layout.SidebarTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * One destination row: icon, then name, then [trailing], such as a [SidebarCount] or a
 * [SidebarExpandButton]. Nested entries are indented by [depth]. The current destination is
 * highlighted. Rows are [PanelTokens.compactRowHeight] tall and follow each other without gaps.
 */
@Composable
public fun SidebarEntry(
    label: String,
    icon: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    depth: Int = 0,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(SidebarTokens.entryRadius)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = PanelTokens.compactRowHeight)
            .selectable(
                selected,
                interactionSource = interactions,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ).clip(shape)
            .background(if (selected) OtusColors.selectedFill else Color.Transparent)
            .controlPressFeedback(interactions, shape)
            .padding(horizontal = SidebarTokens.entryPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SidebarTokens.entryPadding),
    ) {
        Box(Modifier.nestIndent(SidebarTokens.nestIndent, depth)) { LeadingSymbol(icon) }
        Text(
            label,
            Modifier.weight(1f),
            style = panelTextStyle().copy(color = Color.Unspecified),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        trailing()
    }
}

/**
 * A caption over a group of entries that collapses it. [action], such as an icon button that
 * adds an entry, sits at the end.
 */
@Composable
public fun SidebarHeading(
    text: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth().heightIn(min = PanelTokens.touchTarget),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .weight(1f)
                .heightIn(min = PanelTokens.touchTarget)
                .semantics(mergeDescendants = true) {
                    heading()
                    stateDescription = if (expanded) "Expanded" else "Collapsed"
                }.clickable(role = Role.Button) { onExpandedChange(!expanded) }
                .padding(horizontal = SidebarTokens.entryPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PanelTokens.iconGap),
        ) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                SidebarDetail(text)
                ExpandChevron(expanded, size = SidebarTokens.headingChevron)
            }
        }
        action?.invoke()
    }
}

/** A count at the end of a [SidebarEntry], such as its unread mail. */
@Composable
public fun SidebarCount(text: String) {
    SidebarDetail(text)
}

/**
 * Shows or hides the nested entries of the [SidebarEntry] named [label]. Its touch area is 48 dp
 * wide and as tall as a [PanelTokens.compactRowHeight] row, so it never makes a row taller.
 */
@Composable
public fun SidebarExpandButton(
    label: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(PanelTokens.touchTarget, PanelTokens.compactRowHeight)
            .semantics { contentDescription = if (expanded) "Collapse $label" else "Expand $label" }
            .toggleable(
                expanded,
                interactions,
                indication = null,
                role = Role.Button,
                onValueChange = onExpandedChange,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(SidebarTokens.expandButton).controlPressFeedback(interactions),
            contentAlignment = Alignment.Center,
        ) { ExpandChevron(expanded) }
    }
}

@Composable
internal fun SidebarDetail(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        modifier,
        style = panelTextStyle(),
        color = OtusColors.text2,
        fontSize = SidebarTokens.detailSize,
        lineHeight = SidebarTokens.detailLineHeight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
