package com.otus.designsystem.shared.settings.components

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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.shared.controls.elements.LeadingSymbol
import com.otus.designsystem.shared.controls.elements.ToggleSwitch
import com.otus.designsystem.shared.controls.elements.controlPressFeedback
import com.otus.designsystem.shared.feedback.components.InfoText
import com.otus.designsystem.shared.feedback.components.InfoTip
import com.otus.designsystem.shared.people.components.NamedAvatar
import com.otus.designsystem.shared.people.components.PersonText
import com.otus.designsystem.shared.rows.components.RowDensity
import com.otus.designsystem.shared.rows.components.topHairline
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.layout.PageTokens
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.tokens.settings.SettingTokens
import org.jetbrains.compose.resources.DrawableResource
import kotlin.math.min

/**
 * A flat settings row: [title] and an optional [description] beside [trailing], such as a
 * [com.otus.designsystem.shared.settings.elements.ValueButton], a chevron, or a
 * [com.otus.designsystem.shared.rows.elements.RowButton]. A hairline separates it from what is
 * above it, so the first row in its parent, such as at the top of a page, has none. With [onClick]
 * the whole row is one button; without it, only its trailing controls act. A
 * [RowDensity.Compact] row drops the hairline and fits one line in
 * [PanelTokens.compactRowHeight]; a taller trailing control reaches into its padding instead of
 * making it taller.
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
    SettingRow(title, onClick, icon?.let { { LeadingSymbol(it) } }, modifier, description, density, trailing)
}

/** A [SettingRow] with [leading] before its text in place of an icon, such as an avatar. */
@Composable
public fun SettingRow(
    title: String,
    onClick: (() -> Unit)?,
    leading: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    description: String? = null,
    density: RowDensity = RowDensity.Regular,
    trailing: @Composable () -> Unit = {},
) {
    SettingRowLayout(
        RowTitle(title),
        description,
        person = null,
        leading,
        trailing,
        density,
        onClick?.let { modifier.rowButton(it) } ?: modifier,
    )
}

/**
 * A [SettingRow] button whose [description] names [person], with their avatar before the name, such
 * as the person whose mail usually goes to a suggested folder.
 */
@Composable
public fun SettingRow(
    title: String,
    onClick: () -> Unit,
    description: String,
    person: NamedAvatar,
    modifier: Modifier = Modifier,
    icon: DrawableResource? = null,
    density: RowDensity = RowDensity.Regular,
) {
    SettingRowLayout(
        RowTitle(title),
        description,
        person,
        icon?.let { { LeadingSymbol(it) } },
        trailing = {},
        density,
        modifier.rowButton(onClick),
    )
}

/** Makes the whole row one button with the shared press feedback. */
@Composable
private fun Modifier.rowButton(onClick: () -> Unit): Modifier {
    val interactions = remember { MutableInteractionSource() }
    return clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
        .controlPressFeedback(interactions, SmoothCornerShape(SheetTokens.rowRadius))
}

/**
 * A settings row that turns something on or off; the whole row is the switch. An [info] button
 * after the title explains the setting on request, so the [description] can stay one short line.
 */
@Composable
public fun SettingToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: DrawableResource? = null,
    info: InfoText? = null,
) {
    val interactions = remember { MutableInteractionSource() }
    SettingRowLayout(
        RowTitle(title, info),
        description,
        person = null,
        icon?.let { { LeadingSymbol(it) } },
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
                ).controlPressFeedback(interactions, SmoothCornerShape(SheetTokens.rowRadius)),
    )
}

@Composable
private fun SettingRowLayout(
    title: RowTitle,
    description: String?,
    person: NamedAvatar?,
    leading: (@Composable () -> Unit)?,
    trailing: @Composable () -> Unit,
    density: RowDensity,
    modifier: Modifier,
) {
    val compact = density == RowDensity.Compact
    var first by remember { mutableStateOf(false) }
    val separation =
        if (compact) {
            Modifier.heightIn(min = PanelTokens.compactRowHeight)
        } else {
            Modifier
                .onPlaced { first = it.positionInParent().y < 1f }
                .topHairline(OtusColors.bgLight, PageTokens.textInset) { !first }
        }
    val verticalPadding = if (compact) SettingTokens.compactRowPadding else SettingTokens.rowPadding
    Row(
        modifier
            .fillMaxWidth()
            .then(separation)
            .padding(horizontal = PageTokens.textInset, vertical = verticalPadding),
        horizontalArrangement = Arrangement.spacedBy(SettingTokens.rowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SettingTokens.textGap)) {
            SettingTitle(title, compact)
            if (description != null) {
                PersonText(
                    description,
                    person,
                    color = OtusColors.text2,
                    fontSize = SettingTokens.descriptionSize,
                    lineHeight = SettingTokens.descriptionLineHeight,
                )
            }
        }
        // The row pads its content, so the control may only claim the height inside that padding.
        val inner = PanelTokens.compactRowHeight - SettingTokens.compactRowPadding * 2
        if (compact) Box(Modifier.overlapHeight(inner)) { trailing() } else trailing()
    }
}

/**
 * Lays the content out at most [height] tall and centers anything taller across that height, so a
 * row-high control reaches into a compact row's padding, or an info button past its title's line,
 * without making the row taller.
 */
internal fun Modifier.overlapHeight(height: Dp): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
        val shown = min(placeable.height, height.roundToPx())
        layout(placeable.width, shown) { placeable.place(0, (shown - placeable.height) / 2) }
    }

/** A row's title and the [info] button after it, if any. */
private class RowTitle(
    val text: String,
    val info: InfoText? = null,
)

@Composable
private fun SettingTitle(
    title: RowTitle,
    compact: Boolean,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            title.text,
            Modifier.weight(1f, fill = false),
            fontSize = SettingTokens.titleSize,
            lineHeight = SettingTokens.titleLineHeight,
            maxLines = if (compact) 1 else Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis,
        )
        // The button's touch target reaches into the row's padding rather than making it taller.
        if (title.info != null) InfoTip(title.info, Modifier.overlapHeight(PanelTokens.iconSize))
    }
}
