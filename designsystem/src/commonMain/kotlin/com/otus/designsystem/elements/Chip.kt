package com.otus.designsystem.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A compact pill inside a non-overlapping, minimum 48 dp touch target.
 *
 * A null [label] shows only [leading]; give the chip a content description through [modifier].
 * [detail] follows after a short divider line, such as the mailboxes beside an account.
 */
@Composable
public fun Chip(
    label: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    detail: String? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    ChipFrame(onClick, modifier, selected, enabled, OtusColors.bgDark) {
        leading?.invoke()
        label?.let { ChipText(it) }
        detail?.let {
            ChipDivider(selected)
            ChipText(it)
        }
    }
}

/** How a [ChipFrame] lays out its content. */
internal enum class ChipLayout {
    /** Padded text and icons. */
    Regular,

    /** As wide as it is tall, like an [IconButton]. */
    Circle,

    /** A round avatar first, close to the start edge. */
    Avatar,
}

/**
 * The pill and its 48 dp touch target, shared by [Chip], [SidebarPill], [IconPill], and the
 * button pills; [fill] fills it while unselected. A chip with a [selected] state reads as a
 * checkbox; one without, a null, is a button.
 */
@Composable
internal fun ChipFrame(
    onClick: () -> Unit,
    modifier: Modifier,
    selected: Boolean?,
    enabled: Boolean,
    fill: Color,
    layout: ChipLayout = ChipLayout.Regular,
    content: @Composable RowScope.() -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .sizeIn(minWidth = PanelTokens.touchTarget, minHeight = PanelTokens.touchTarget)
            .then(
                if (selected == null) {
                    Modifier.clickable(interactions, null, enabled, role = Role.Button, onClick = onClick)
                } else {
                    Modifier.toggleable(selected, interactions, null, enabled, Role.Checkbox) { onClick() }
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.reportControlVisualBounds().controlPressFeedback(interactions),
            shape = CircleShape,
            color = if (selected == true) OtusColors.accentDark else fill,
            contentColor = if (enabled) OtusColors.text1 else OtusColors.text3,
            border =
                BorderStroke(
                    PanelTokens.borderWidth,
                    if (selected == true) OtusColors.accentLight else OtusColors.strokeBgDark,
                ),
        ) {
            Row(
                if (layout == ChipLayout.Circle) {
                    Modifier.size(PanelTokens.chipHeight)
                } else {
                    Modifier
                        .heightIn(min = PanelTokens.chipHeight)
                        .padding(
                            start = if (layout == ChipLayout.Avatar) PanelTokens.iconGap else PanelTokens.chipPadding,
                            end = PanelTokens.chipPadding,
                            top = PanelTokens.iconGap,
                            bottom = PanelTokens.iconGap,
                        )
                },
                horizontalArrangement = Arrangement.spacedBy(PanelTokens.iconGap, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

@Composable
internal fun ChipText(text: String) {
    Text(text, style = panelTextStyle().copy(color = Color.Unspecified), maxLines = 1)
}

/** A thin line, shorter than the text beside it, in a color that recedes behind that text. */
@Composable
private fun ChipDivider(selected: Boolean) {
    Box(
        Modifier
            .padding(horizontal = PanelTokens.iconGap)
            .size(PanelTokens.borderWidth, PanelTokens.chipDividerHeight)
            .background(if (selected) OtusColors.accentLight else OtusColors.bgLight),
    )
}
