package com.otus.designsystem.elements

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
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SidebarTokens
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * The sidebar's main destination, such as Inbox, as a large row: icon, a SemiBold label, and an
 * optional [count] at the end. It has a `bgRegular` fill, or `accentDark` when it is the current
 * destination.
 */
@Composable
public fun SidebarInboxEntry(
    label: String,
    icon: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: String? = null,
) {
    val interactions = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(SidebarTokens.entryRadius)
    Row(
        modifier
            // Inset like a SidebarDivider, so the row's edges line up with the hairlines.
            .padding(horizontal = SidebarTokens.entryPadding)
            .fillMaxWidth()
            .heightIn(min = SidebarTokens.inboxHeight)
            .selectable(selected, interactions, indication = null, role = Role.Tab, onClick = onClick)
            .clip(shape)
            .background(if (selected) OtusColors.accentDark else OtusColors.bgRegular)
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
        count?.let { SidebarCount(it) }
    }
}
