package com.otus.designsystem.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * Labelled single-line form input, with an optional `text2` [icon] before the text, such as a
 * note, and with [onRemove], a ✕ at its end that removes the field.
 */
@Composable
public fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    supportingText: String? = null,
    kind: InputKind = InputKind.Text,
    icon: DrawableResource? = null,
    onRemove: (() -> Unit)? = null,
) {
    val style = panelTextStyle()
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(PanelTokens.iconGap)) {
        FieldLabel(label)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            singleLine = true,
            textStyle = style,
            cursorBrush = SolidColor(OtusColors.accentLight),
            visualTransformation =
                if (kind ==
                    InputKind.Password
                ) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
            keyboardOptions =
                KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = kind.keyboardType,
                ),
            decorationBox = { field ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = PanelTokens.touchTarget)
                        .background(OtusColors.bgDark, RoundedCornerShape(SheetTokens.rowRadius))
                        .border(
                            BorderStroke(PanelTokens.borderWidth, OtusColors.strokeBgDark),
                            RoundedCornerShape(SheetTokens.rowRadius),
                            // A ✕'s touch target spaces it from the edge, so the field keeps less inset there.
                        ).padding(
                            start = SheetTokens.gap,
                            end =
                                if (onRemove ==
                                    null
                                ) {
                                    SheetTokens.gap
                                } else {
                                    PanelTokens.iconGap
                                },
                        ),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        icon?.let {
                            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                                Box(Modifier.padding(end = SheetTokens.gap)) { Symbol(it) }
                            }
                        }
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(
                                    placeholder,
                                    style = style,
                                    color = OtusColors.text3,
                                    maxLines = 1,
                                )
                            }
                            field()
                        }
                        onRemove?.let { RemoveFieldButton("Remove $label", it) }
                    }
                }
            },
        )
        supportingText?.let {
            Text(
                it,
                Modifier.padding(horizontal = SheetTokens.gap),
                style = style,
                fontSize = SheetTokens.detailSize,
                color = OtusColors.text2,
            )
        }
    }
}
