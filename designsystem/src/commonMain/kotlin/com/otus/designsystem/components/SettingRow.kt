package com.otus.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.ToggleSwitch
import com.otus.designsystem.elements.controlPressFeedback
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.SettingTokens
import com.otus.designsystem.theme.SheetTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * A flat settings row: [title] and an optional [description] beside [trailing], such as a
 * [com.otus.designsystem.elements.ValueButton], a chevron, or a
 * [com.otus.designsystem.elements.RowButton]. A hairline separates it from the row above. With
 * [onClick] the whole row is one button; without it, only its trailing controls act.
 */
@Composable
public fun SettingRow(
    title: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: DrawableResource? = null,
    trailing: @Composable () -> Unit = {},
) {
    val interactions = remember { MutableInteractionSource() }
    SettingRowLayout(
        title,
        description,
        icon,
        trailing,
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
) {
    val interactions = remember { MutableInteractionSource() }
    SettingRowLayout(
        title,
        description,
        icon = null,
        trailing = { ToggleSwitch(checked) },
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
    modifier: Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .topHairline(OtusColors.bgLight, PageTokens.textInset)
            .padding(horizontal = PageTokens.textInset, vertical = SettingTokens.rowPadding),
        horizontalArrangement = Arrangement.spacedBy(SettingTokens.rowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let { Symbol(it) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SettingTokens.textGap)) {
            Text(title, fontSize = SettingTokens.titleSize, lineHeight = SettingTokens.titleLineHeight)
            description?.let {
                Text(
                    it,
                    color = OtusColors.text2,
                    fontSize = SettingTokens.descriptionSize,
                    lineHeight = SettingTokens.descriptionLineHeight,
                )
            }
        }
        trailing()
    }
}
