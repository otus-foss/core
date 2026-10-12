package com.otus.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.ToggleSwitch
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.SettingTokens
import com.otus.designsystem.theme.SheetTokens
import org.jetbrains.compose.resources.DrawableResource
import kotlin.math.min

/**
 * A flat settings row: [title] and an optional [description] beside [trailing], such as a
 * [com.otus.designsystem.elements.ValueButton], a chevron, or a
 * [com.otus.designsystem.elements.RowButton]. A hairline separates it from the row above. With
 * [onClick] the whole row is one button; without it, only its trailing controls act. A
 * [RowDensity.Compact] row drops the hairline and fits one line in about 44 dp; a 48 dp trailing
 * control overlaps its edges instead of making it taller.
 */
@Composable
public fun SettingRow(
    title: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: DrawableResource? = null,
    density: RowDensity = RowDensity.Regular,
    trailing: @Composable () -> Unit = {},
) {
    val interactions = remember { MutableInteractionSource() }
    SettingRowLayout(
        title,
        description,
        icon,
        trailing,
        density,
        onClick?.let {
            modifier
                .clickable(interactions, indication = null, role = Role.Button, onClick = it)
                .controlPressFeedback(interactions, RoundedCornerShape(SheetTokens.rowRadius))
        } ?: modifier,
    )
}

/** A settings row that turns something on or off; the whole row is the switch. */
@Composable
public fun SettingToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: DrawableResource? = null,
) {
    val interactions = remember { MutableInteractionSource() }
    SettingRowLayout(
        title,
        description,
        icon,
        trailing = { ToggleSwitch(checked) },
        density = RowDensity.Regular,
        modifier =
            modifier
                .toggleable(
                    checked,
                    interactions,
                    indication = null,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                ).controlPressFeedback(interactions, RoundedCornerShape(SheetTokens.rowRadius)),
    )
}

@Composable
private fun SettingRowLayout(
    title: String,
    description: String?,
    icon: DrawableResource?,
    trailing: @Composable () -> Unit,
    density: RowDensity,
    modifier: Modifier,
) {
    val compact = density == RowDensity.Compact
    Row(
        modifier
            .fillMaxWidth()
            .then(
                if (compact) {
                    Modifier.heightIn(min = SettingTokens.compactRowHeight)
                } else {
                    Modifier.topHairline(OtusColors.bgLight, PageTokens.textInset)
                },
            ).padding(
                horizontal = PageTokens.textInset,
                vertical = if (compact) SettingTokens.compactRowPadding else SettingTokens.rowPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(SettingTokens.rowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let { Symbol(it) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SettingTokens.textGap)) {
            Text(
                title,
                fontSize = SettingTokens.titleSize,
                lineHeight = SettingTokens.titleLineHeight,
                maxLines = if (compact) 1 else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
            )
            description?.let {
                Text(
                    it,
                    color = OtusColors.text2,
                    fontSize = SettingTokens.descriptionSize,
                    lineHeight = SettingTokens.descriptionLineHeight,
                )
            }
        }
        // The row pads its content, so the control may only claim the height inside that padding.
        val inner = SettingTokens.compactRowHeight - SettingTokens.compactRowPadding * 2
        if (compact) Box(Modifier.overlapHeight(inner)) { trailing() } else trailing()
    }
}

/**
 * Lays the content out at most [height] tall and centers anything taller across that height, so a
 * 48 dp control reaches into a compact row's padding without making the row taller.
 */
private fun Modifier.overlapHeight(height: Dp): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
        val shown = min(placeable.height, height.roundToPx())
        layout(placeable.width, shown) { placeable.place(0, (shown - placeable.height) / 2) }
    }
