package com.otus.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.offset
import com.otus.designsystem.elements.IconButton
import com.otus.designsystem.elements.InputKind
import com.otus.designsystem.elements.RemoveFieldButton
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_star
import com.otus.designsystem.resources.symbol_star_fill
import com.otus.designsystem.resources.symbol_unfold_more
import com.otus.designsystem.theme.ContactTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * One editable address or number of a contact: the field, a [label] button that opens
 * [labelChoices] in a menu, such as Work or Personal, and, when [preferred] is not null, a star that
 * makes this the value Mail uses first. The preferred field fills with the accent so it stands out
 * among the others. [description] names the field to screen readers, such as "Email 2". An
 * [error], such as an address that is not valid, shows below the field once focus leaves it, so
 * it never interrupts typing. An [icon], such as a phone, names the kind of value as the contact
 * page does, and [onRemove] puts a ✕ at the end that removes the field.
 */
@Composable
public fun ContactFieldRow(
    value: String,
    onValueChange: (String) -> Unit,
    description: String,
    label: String,
    labelChoices: List<OptionsItem>,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    kind: InputKind = InputKind.Text,
    preferred: Boolean? = null,
    onPreferredClick: () -> Unit = {},
    error: String? = null,
    icon: DrawableResource? = null,
    onRemove: (() -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val style = panelTextStyle()
    val shape = RoundedCornerShape(SheetTokens.rowRadius)
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = ContactTokens.fieldHeight)
                .background(if (preferred == true) OtusColors.accentDark else OtusColors.bgDark, shape)
                .border(
                    BorderStroke(
                        PanelTokens.borderWidth,
                        if (preferred == true) OtusColors.accentRegular else OtusColors.strokeBgDark,
                    ),
                    shape,
                )
                // A trailing button's touch target spaces it from the edge; without one, the label needs the inset.
                .padding(
                    start = SheetTokens.inset,
                    end = if (preferred == null && onRemove == null) SheetTokens.inset else PanelTokens.iconGap,
                ),
            horizontalArrangement = Arrangement.spacedBy(PanelTokens.iconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                    Box(Modifier.padding(end = SheetTokens.gap - PanelTokens.iconGap)) { Symbol(it) }
                }
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier =
                    Modifier
                        .weight(1f)
                        .onFocusChanged { focused = it.isFocused }
                        .semantics { contentDescription = description },
                singleLine = true,
                textStyle = style,
                cursorBrush = SolidColor(OtusColors.accentLight),
                keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = kind.keyboardType),
                decorationBox = { field ->
                    if (value.isEmpty()) Text(placeholder, style = style, color = OtusColors.text3, maxLines = 1)
                    field()
                },
            )
            LabelButton(label, description, labelChoices)
            // The star and ✕ keep 48 dp targets that overlap and reach past the group, so the icons
            // themselves sit a row gap apart and as far from the edge as the text is.
            Row(
                Modifier.bleed(ContactTokens.trailingBleedStart, ContactTokens.trailingBleedEnd),
                horizontalArrangement = Arrangement.spacedBy(-ContactTokens.trailingOverlap),
            ) {
                preferred?.let { on ->
                    IconButton(
                        if (on) "Primary" else "Make primary",
                        onPreferredClick,
                        outlined = false,
                        fill = Color.Transparent,
                    ) {
                        // accentLight is too faint on the accent fill, so the filled star takes text1.
                        val tint = if (on) OtusColors.text1 else OtusColors.text2
                        CompositionLocalProvider(LocalContentColor provides tint) {
                            Symbol(if (on) Res.drawable.symbol_star_fill else Res.drawable.symbol_star)
                        }
                    }
                }
                onRemove?.let { RemoveFieldButton("Remove $description", it) }
            }
        }
        if (error != null && !focused) {
            Text(
                error,
                Modifier.padding(start = SheetTokens.inset, top = PanelTokens.iconGap),
                style = panelTextStyle(),
                color = OtusColors.text2,
                fontSize = ContactTokens.detailSize,
            )
        }
    }
}

/** The label of a [ContactFieldRow] as a small outlined pill that opens [choices] below it. */
@Composable
private fun LabelButton(
    label: String,
    field: String,
    choices: List<OptionsItem>,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier
                .height(ContactTokens.labelChipHeight)
                .border(PanelTokens.borderWidth, OtusColors.bgLight, CircleShape)
                .clickable(role = Role.Button) { open = true }
                .semantics { contentDescription = "$field label: $label. Change" }
                .padding(start = PanelTokens.chipPadding, end = PanelTokens.iconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = panelTextStyle(), color = OtusColors.text2, fontSize = ContactTokens.detailSize)
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                Symbol(Res.drawable.symbol_unfold_more, size = PanelTokens.iconSize * 0.8f)
            }
        }
        if (open) OptionsMenu(choices) { open = false }
    }
}

/** Lays this out [start] and [end] wider than the room it takes, reaching past it on both sides. */
private fun Modifier.bleed(
    start: Dp,
    end: Dp,
): Modifier =
    layout { measurable, constraints ->
        val before = start.roundToPx()
        val after = end.roundToPx()
        val placeable = measurable.measure(constraints.offset(horizontal = before + after))
        layout(placeable.width - before - after, placeable.height) { placeable.place(-before, 0) }
    }
