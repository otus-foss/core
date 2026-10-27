package com.otus.designsystem.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SidebarTokens
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * One mailbox in a [com.otus.designsystem.components.SidebarMailboxGrid]: [icon] over a small
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
                count?.let {
                    TileBadge(it, ring = fill, color = OtusColors.accentDark)
                }
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

/** A count in a pill that hangs off the icon's corner, ringed in the tile's [ring] color. */
@Composable
private fun TileBadge(
    text: String,
    ring: Color,
    color: Color,
) {
    Box(
        Modifier
            .heightIn(min = SidebarTokens.badgeHeight)
            .widthIn(min = SidebarTokens.badgeHeight)
            .ringedFill(ring, color)
            .padding(horizontal = SidebarTokens.badgePadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = panelTextStyle(),
            fontSize = SidebarTokens.badgeSize,
            lineHeight = SidebarTokens.badgeSize,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

/**
 * A pill in [ring], with [fill] inset by the ring's width. The fill never reaches the pill's edge,
 * so no sliver of it shows past the ring's anti-aliased outline, and the pill keeps its size.
 */
private fun Modifier.ringedFill(
    ring: Color,
    fill: Color,
): Modifier =
    drawBehind {
        val inset = SidebarTokens.badgeRing.toPx()
        val radius = size.height / 2
        drawRoundRect(ring, cornerRadius = CornerRadius(radius))
        drawRoundRect(
            fill,
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            cornerRadius = CornerRadius(radius - inset),
        )
    }
