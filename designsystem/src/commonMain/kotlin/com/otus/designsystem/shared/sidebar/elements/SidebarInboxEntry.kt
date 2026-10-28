package com.otus.designsystem.shared.sidebar.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.SidebarTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * The sidebar's main destination, such as Inbox, as a large row: icon, a SemiBold label, and an
 * optional [count] at the end. It has a `bgRegular` fill, or `selectedFill` when it is the current
 * destination. While [inset], its edges line up with a SidebarDivider and a SidebarMailboxGrid,
 * and [count] takes the tiles' badge; otherwise it spans the full width, like a [SidebarEntry], so
 * its icon and count line up with theirs. The caller shortens a large badge count, such as "999+".
 */
@Composable
public fun SidebarInboxEntry(
    label: String,
    icon: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: String? = null,
    inset: Boolean = true,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(SidebarTokens.entryRadius)
    val fill = if (selected) OtusColors.selectedFill else OtusColors.bgRegular
    Row(
        modifier
            .padding(horizontal = if (inset) SidebarTokens.entryPadding else 0.dp)
            .fillMaxWidth()
            .heightIn(min = SidebarTokens.inboxHeight)
            .selectable(selected, interactions, indication = null, role = Role.Tab, onClick = onClick)
            .clip(shape)
            .background(fill)
            .controlPressFeedback(interactions, shape)
            .padding(horizontal = SidebarTokens.entryPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SidebarTokens.entryPadding),
    ) {
        Symbol(icon)
        Text(
            label,
            Modifier.weight(1f),
            style = panelTextStyle(),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        count?.let { if (inset) SidebarBadge(it, surface = fill) else SidebarCount(it) }
    }
}
