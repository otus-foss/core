package com.otusfoss.core.designsystem.shared.settings.components

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
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
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
import com.otusfoss.core.designsystem.shared.controls.elements.LeadingSymbol
import com.otusfoss.core.designsystem.shared.controls.elements.ToggleSwitch
import com.otusfoss.core.designsystem.shared.controls.elements.controlPressFeedback
import com.otusfoss.core.designsystem.shared.feedback.components.InfoText
import com.otusfoss.core.designsystem.shared.feedback.components.InfoTip
import com.otusfoss.core.designsystem.shared.people.components.NamedAvatar
import com.otusfoss.core.designsystem.shared.people.components.PersonText
import com.otusfoss.core.designsystem.shared.rows.components.RowDensity
import com.otusfoss.core.designsystem.shared.rows.components.topHairline
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens
import com.otusfoss.core.designsystem.theme.tokens.settings.SettingTokens
import org.jetbrains.compose.resources.DrawableResource
import kotlin.math.min

/**
 * A flat settings row: [title] and an optional [description] beside [trailing], such as a
 * [com.otusfoss.core.designsystem.shared.settings.elements.ValueButton], a chevron, or a
 * [com.otusfoss.core.designsystem.shared.rows.elements.RowButton]. A hairline separates it from what is
 * above it, so the first row in its parent, such as at the top of a page, has none. With [onClick]
 * the whole row is one button; without it, only its trailing controls act. A
 * [RowDensity.Compact] row drops the hairline and fits one line in
 * [Sizes.compactRowHeight]; a taller trailing control reaches into its padding instead of
 * making it taller. Inside [DependentSettings] that is not enabled, the row is dimmed and ignores
 * taps.
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
    val enabled = LocalSettingsEnabled.current
    val leading: (@Composable () -> Unit)? = icon?.let { { LeadingSymbol(it, enabled = enabled) } }
    SettingRow(title, onClick, leading, modifier, description, density, trailing)
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

/** Makes the whole row one button with the shared press feedback; disabled [DependentSettings] ignore taps. */
@Composable
private fun Modifier.rowButton(onClick: () -> Unit): Modifier {
    val interactions = remember { MutableInteractionSource() }
    val enabled = LocalSettingsEnabled.current
    return clickable(interactions, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
        .controlPressFeedback(interactions, SmoothCornerShape(SheetTokens.rowRadius))
}

/**
 * How tall a [SettingToggleRow] is, for a list that packs its rows, such as one inside a card. A
 * local, because the row's own parameters are already as many as a function should take.
 */
public val LocalToggleRowDensity: ProvidableCompositionLocal<RowDensity> = compositionLocalOf { RowDensity.Regular }

/**
 * A settings row that turns something on or off; the whole row is the switch. An [info] button
 * after the title explains the setting on request, so the [description] can stay one short line.
 * Inside [DependentSettings] that is not enabled, its switch keeps its position, dimmed. Its
 * height follows [LocalToggleRowDensity].
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
    val enabled = LocalSettingsEnabled.current
    SettingRowLayout(
        RowTitle(title, info),
        description,
        person = null,
        icon?.let { { LeadingSymbol(it, enabled = enabled) } },
        trailing = { ToggleSwitch(checked, enabled = enabled) },
        density = LocalToggleRowDensity.current,
        modifier =
            modifier
                .toggleable(
                    checked,
                    interactions,
                    indication = null,
                    enabled = enabled,
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
            Modifier.heightIn(min = Sizes.compactRowHeight)
        } else {
            Modifier
                .onPlaced { first = it.positionInParent().y < 1f }
                .topHairline(OtusColors.bgLight, PageTokens.textInset) { !first }
        }
    val verticalPadding = if (compact) SettingTokens.compactRowPadding else SettingTokens.rowPadding
    Row(
        modifier
            .foundFlash(title.text)
            .fillMaxWidth()
            .then(separation)
            .padding(horizontal = PageTokens.textInset, vertical = verticalPadding),
        horizontalArrangement = Arrangement.spacedBy(SettingTokens.rowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // A disabled row dims everything but its description, which says what it waits on.
        val content = if (LocalSettingsEnabled.current) LocalContentColor.current else OtusColors.text3
        CompositionLocalProvider(LocalContentColor provides content) {
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
            val inner = Sizes.compactRowHeight - SettingTokens.compactRowPadding * 2
            if (compact) Box(Modifier.overlapHeight(inner)) { trailing() } else trailing()
        }
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
        if (title.info != null) InfoTip(title.info, Modifier.overlapHeight(Sizes.iconSize))
    }
}
