package com.otus.designsystem.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.otus.designsystem.theme.ComposeTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * One header line of a message being written, such as To or Subject: [label] in `text2` beside a
 * borderless single-line field, with a hairline below. [kind] picks the keyboard; text fields
 * also capitalize sentences. [placeholder] shows in `text3` while it is empty.
 */
@Composable
public fun ComposeField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    kind: InputKind = InputKind.Text,
    placeholder: String? = null,
) {
    val style = panelTextStyle()
    val text = kind == InputKind.Text
    ComposeLine(label, modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f).semantics { contentDescription = label },
            singleLine = true,
            textStyle = style,
            cursorBrush = SolidColor(OtusColors.accentLight),
            keyboardOptions =
                KeyboardOptions(
                    capitalization = if (text) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
                    autoCorrectEnabled = text,
                    keyboardType = kind.keyboardType,
                    imeAction = ImeAction.Next,
                ),
            decorationBox = { field ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty() &&
                        placeholder != null
                    ) {
                        Text(placeholder, style = style, color = OtusColors.text3)
                    }
                    field()
                }
            },
        )
    }
}

/**
 * The shared layout: a 48 dp line with the label in its column first and a hairline below, for
 * these fields and [com.otus.designsystem.components.RecipientLine].
 */
@Composable
internal fun ComposeLine(
    label: String,
    modifier: Modifier,
    value: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = PanelTokens.touchTarget)
            .bottomHairline()
            .padding(horizontal = PageTokens.textInset),
        horizontalArrangement = Arrangement.spacedBy(ComposeTokens.labelGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The field names itself, so the label is not read twice.
        Text(
            label,
            Modifier.widthIn(min = ComposeTokens.labelWidth).clearAndSetSemantics {},
            style = panelTextStyle(),
            color = OtusColors.text2,
        )
        value()
    }
}

private fun Modifier.bottomHairline(): Modifier =
    drawBehind {
        val y = size.height - ComposeTokens.hairlineWidth.toPx() / 2
        drawLine(OtusColors.bgLight, Offset(0f, y), Offset(size.width, y), ComposeTokens.hairlineWidth.toPx())
    }
