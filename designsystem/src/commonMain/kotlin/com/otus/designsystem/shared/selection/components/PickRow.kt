package com.otus.designsystem.shared.selection.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.rows.components.RowDensity
import com.otus.designsystem.theme.colors.OtusAlpha
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.core.Spacing
import com.otus.designsystem.theme.tokens.overlays.MenuTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.tokens.selection.PickTokens
import com.otus.designsystem.theme.typography.TypeScale
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * A choice in a list, sheet, or menu: an optional [leading] avatar or icon, the one-line [title], a
 * smaller one-line `text2` [subtitle], and the [mark]. A null [onClick] shows the row disabled. A
 * [RowDensity.Compact] row is one touch target tall; a regular one takes the height its mark's
 * style asks for.
 */
@Composable
public fun PickRow(
    title: String,
    subtitle: String?,
    mark: PickMark,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    density: RowDensity = RowDensity.Regular,
    leading: (@Composable () -> Unit)? = null,
) {
    val height = if (density == RowDensity.Compact) Sizes.touchTarget else mark.style.rowHeight(subtitle != null)
    PickRowFrame(mark, onClick, modifier.heightIn(min = height), leading) { PickRowText(title, subtitle, density) }
}

/**
 * A person to pick: the [avatar], the [name], and a one-line `text3` [address] at the end when it
 * adds to the name.
 */
@Composable
public fun PersonPickRow(
    name: String,
    address: String?,
    mark: PickMark,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatar: @Composable () -> Unit,
) {
    PickRowFrame(
        mark,
        onClick,
        modifier.heightIn(min = mark.style.rowHeight(hasSubtitle = false)),
        avatar,
        trailing = address?.let { { PickDetail(it) } },
    ) { PickRowText(name, subtitle = null, RowDensity.Compact) }
}

/**
 * The [title] and smaller `text2` [subtitle] of a [PickRow]. A [RowDensity.Regular] row wraps its
 * subtitle, which can explain a choice in a few sentences; a compact one keeps it to a line.
 */
@Composable
private fun PickRowText(
    title: String,
    subtitle: String?,
    density: RowDensity,
) {
    Text(title, style = panelTextStyle(), maxLines = 1, overflow = TextOverflow.Ellipsis)
    if (subtitle != null) {
        Text(
            subtitle,
            style = panelTextStyle(),
            color = OtusColors.text2,
            fontSize = TypeScale.detail,
            maxLines = if (density == RowDensity.Compact) 1 else Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PickDetail(text: String) {
    Text(
        text,
        Modifier.widthIn(max = MenuTokens.detailMaxWidth),
        style = panelTextStyle(),
        color = OtusColors.text3,
        fontSize = TypeScale.detail,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * The frame of a [PickRow] for rows whose text is not a plain title: the [leading] slot, the
 * [content] column, an optional [trailing] slot, and the [mark] at the end, or first when
 * [PickMark.atStart]. [modifier] sets the row's least height, and [verticalPadding] keeps a tall
 * row's text off its edges.
 */
@Composable
internal fun PickRowFrame(
    mark: PickMark,
    onClick: (() -> Unit)?,
    modifier: Modifier,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)? = null,
    verticalPadding: Dp = Spacing.iconGap,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    val card = mark.style == PickStyle.Card
    val shape: Shape = if (card) SmoothCornerShape(SheetTokens.rowRadius) else RectangleShape
    Row(
        modifier
            .fillMaxWidth()
            .pick(mark, onClick, interactions)
            .alpha(if (onClick != null) 1f else OtusAlpha.DISABLED)
            .clip(shape)
            .cardFill(card, mark.selected)
            .controlPressFeedback(interactions, shape)
            .padding(horizontal = if (card) SheetTokens.gap else SheetTokens.inset, vertical = verticalPadding),
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (mark.atStart) PickMarkView(mark)
        leading?.invoke()
        Column(Modifier.weight(1f), content = content)
        trailing?.invoke()
        if (!mark.atStart) PickMarkView(mark)
    }
}

private fun Modifier.pick(
    mark: PickMark,
    onClick: (() -> Unit)?,
    interactions: MutableInteractionSource,
): Modifier =
    if (mark.multiple) {
        toggleable(mark.selected, interactions, indication = null, enabled = onClick != null, role = Role.Checkbox) {
            onClick?.invoke()
        }
    } else {
        selectable(mark.selected, interactions, indication = null, enabled = onClick != null, role = Role.RadioButton) {
            onClick?.invoke()
        }
    }

private fun Modifier.cardFill(
    card: Boolean,
    selected: Boolean,
): Modifier = if (card) background(if (selected) OtusColors.selectedFill else OtusColors.bgDark) else this

/** A row's least height for its style: a [PickStyle.Card] row is a touch target tall until it has a subtitle. */
internal fun PickStyle.rowHeight(hasSubtitle: Boolean): Dp =
    when (this) {
        PickStyle.Mark -> SheetTokens.rowHeight
        PickStyle.CheckCircle, PickStyle.Check -> PickTokens.optionHeight
        PickStyle.Card -> if (hasSubtitle) SheetTokens.rowHeight else Sizes.touchTarget
    }
