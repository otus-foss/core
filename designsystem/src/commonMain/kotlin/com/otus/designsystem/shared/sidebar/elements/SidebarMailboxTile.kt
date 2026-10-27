package com.otus.designsystem.shared.sidebar.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SidebarTokens
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * One mailbox in a [com.otus.designsystem.shared.sidebar.components.SidebarMailboxGrid]: [icon] over a small
 * [label], with [count] in a badge on the icon's top-end corner. The caller shortens a large
 * count, such as "999+". The current mailbox gets a `selectedFill` fill.
 */
@Composable
public fun SidebarMailboxTile(
    label: String,
    icon: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: String? = null,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(SidebarTokens.entryRadius)
    val fill = if (selected) OtusColors.selectedFill else OtusColors.bgRegular
    Column(
        modifier
            .fillMaxWidth()
            .heightIn(min = SidebarTokens.tileHeight)
            .selectable(selected, interactions, indication = null, role = Role.Tab, onClick = onClick)
            // Inner semantics are replaced; the click and selected state above stay.
            .clearAndSetSemantics { contentDescription = listOfNotNull(label, count).joinToString(", ") }
            .clip(shape)
            .background(fill)
            .controlPressFeedback(interactions, shape),
        verticalArrangement = Arrangement.spacedBy(SidebarTokens.tileLabelGap, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Symbol(icon)
            // A zero-size anchor on the icon's corner centers the badge there without resizing the icon.
            Box(Modifier.align(Alignment.TopEnd).size(0.dp).wrapContentSize(unbounded = true)) {
                count?.let { SidebarBadge(it, surface = fill) }
            }
        }
        Text(
            label,
            Modifier.padding(horizontal = SidebarTokens.inset),
            style = panelTextStyle(),
            fontSize = SidebarTokens.tileLabelSize,
            lineHeight = SidebarTokens.tileLabelLineHeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
