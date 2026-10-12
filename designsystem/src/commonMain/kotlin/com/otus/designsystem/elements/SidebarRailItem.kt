package com.otus.designsystem.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_add
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SidebarTokens
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/** An account in the sidebar rail: its [initials] in a circle, with an optional unread [badge]. */
@Composable
public fun SidebarRailAccount(
    initials: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    RailItem(description, selected, onClick, modifier, badge) {
        Avatar(
            initials,
            size = SidebarTokens.railAvatar,
            fill = railFill(selected),
            ring = railBorder(selected),
        )
    }
}

/** A rail item drawn as [icon] in a circle with a short [label] under it, such as every account at once. */
@Composable
public fun SidebarRailIcon(
    icon: DrawableResource,
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    RailItem(description, selected, onClick, modifier, badge, label) {
        Box(
            Modifier
                .size(SidebarTokens.railAvatar)
                .clip(CircleShape)
                .background(railFill(selected))
                .then(railBorder(selected)?.let { Modifier.border(it, CircleShape) } ?: Modifier),
            contentAlignment = Alignment.Center,
        ) { Symbol(icon) }
    }
}

/** Adds an account: a plus in a dashed circle. */
@Composable
public fun SidebarRailAdd(
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(PanelTokens.touchTarget)
            .semantics { contentDescription = description }
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(SidebarTokens.railAvatar)
                .dashedCircle(OtusColors.bgLight)
                .controlPressFeedback(interactions, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) { Symbol(Res.drawable.symbol_add) }
        }
    }
}

/** A short line between the rail's groups, such as every account at once and each account. */
@Composable
public fun SidebarRailDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(vertical = SidebarTokens.railGap)
            .size(SidebarTokens.railDividerWidth, PanelTokens.borderWidth)
            .background(OtusColors.bgLight),
    )
}

/**
 * A 48 dp target around a circle, with [badge] at its top end. The selected item gets an
 * outline a little outside its circle.
 */
@Composable
private fun RailItem(
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    badge: String?,
    label: String? = null,
    circle: @Composable () -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    Column(
        modifier
            .semantics(mergeDescendants = true) { contentDescription = description }
            .selectable(selected, interactions, indication = null, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(PanelTokens.touchTarget), contentAlignment = Alignment.Center) {
            if (selected) {
                Box(
                    Modifier
                        .size(
                            SidebarTokens.railAvatar +
                                (SidebarTokens.selectionOffset + SidebarTokens.selectionOutline) * 2,
                        ).border(SidebarTokens.selectionOutline, OtusColors.accentLight, CircleShape),
                )
            }
            Box(Modifier.clip(CircleShape).controlPressFeedback(interactions, CircleShape)) { circle() }
            badge?.let { RailBadge(it, Modifier.align(Alignment.TopEnd).offset(x = SidebarTokens.badgeRing)) }
        }
        label?.let {
            Text(
                it,
                style = panelTextStyle(),
                fontSize = SidebarTokens.railLabelSize,
                lineHeight = SidebarTokens.railLabelLineHeight,
                maxLines = 1,
            )
        }
    }
}

/** An unread count in a pill, ringed in the surface color so it stands clear of the circle. */
@Composable
private fun RailBadge(
    text: String,
    modifier: Modifier,
) {
    val shape = CircleShape
    Box(
        modifier
            .heightIn(min = SidebarTokens.badgeHeight)
            .widthIn(min = SidebarTokens.badgeHeight)
            .clip(shape)
            .background(OtusColors.accentDark)
            .border(SidebarTokens.badgeRing, OtusColors.bgDark, shape)
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

private fun railFill(selected: Boolean): Color = if (selected) OtusColors.accentDark else OtusColors.bgRegular

/** The selected item has its outline instead. */
private fun railBorder(selected: Boolean): BorderStroke? =
    BorderStroke(PanelTokens.borderWidth, OtusColors.bgLight).takeUnless { selected }

private fun Modifier.dashedCircle(color: Color): Modifier =
    drawBehind {
        val width = PanelTokens.borderWidth.toPx()
        val dash = SidebarTokens.dashLength.toPx()
        drawCircle(
            color,
            radius = size.minDimension / 2 - width / 2,
            style = Stroke(width, pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash))),
        )
    }
