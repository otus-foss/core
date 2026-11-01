package com.otus.designsystem.shared.input.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.panels.elements.darkSurface
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * The rounded box every form field draws: `bgDark` inside a `strokeBgDark` outline, a touch target
 * tall. A field that [endsWithButton] keeps less inset at its end, since the button's touch target
 * already spaces it from the edge.
 */
internal fun Modifier.inputFrame(endsWithButton: Boolean): Modifier {
    val shape = SmoothCornerShape(SheetTokens.rowRadius)
    return fillMaxWidth()
        .heightIn(min = PanelTokens.touchTarget)
        .darkSurface(shape)
        .padding(start = SheetTokens.gap, end = if (endsWithButton) PanelTokens.iconGap else SheetTokens.gap)
}

/** A field's `text2` icon before its text, such as a note's. */
@Composable
internal fun InputIcon(icon: DrawableResource) {
    CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
        Box(Modifier.padding(end = SheetTokens.gap)) { Symbol(icon) }
    }
}

/**
 * A single-line text field in an [inputFrame], read to screen readers as [description], with
 * [look]'s placeholder and icon, and with [onRemove], a ✕ at its end that removes the field.
 */
@Composable
internal fun InputBox(
    value: String,
    onValueChange: (String) -> Unit,
    description: String,
    look: InputLook,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null,
) {
    val style = panelTextStyle()
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            modifier.fillMaxWidth().semantics {
                contentDescription = description
                val type = look.kind.contentType
                if (type != null) contentType = type
            },
        singleLine = true,
        textStyle = style,
        cursorBrush = SolidColor(OtusColors.accentLight),
        visualTransformation =
            if (look.kind == InputKind.Password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = look.kind.keyboardType),
        decorationBox = { field ->
            Row(Modifier.inputFrame(onRemove != null), verticalAlignment = Alignment.CenterVertically) {
                val icon = look.icon
                if (icon != null) InputIcon(icon)
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) Text(look.placeholder, style = style, color = OtusColors.text3, maxLines = 1)
                    field()
                }
                if (onRemove != null) RemoveFieldButton("Remove $description", onRemove)
            }
        },
    )
}
