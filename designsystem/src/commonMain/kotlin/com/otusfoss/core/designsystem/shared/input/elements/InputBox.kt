package com.otusfoss.core.designsystem.shared.input.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_check
import com.otusfoss.core.designsystem.resources.symbol_error
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.controls.elements.controlPressFeedback
import com.otusfoss.core.designsystem.theme.colors.FolderColor
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.shapes.darkSurface
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.core.Spacing
import com.otusfoss.core.designsystem.theme.tokens.overlays.SheetTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * The rounded box every form field draws: `bgDark` inside a `strokeBgDark` outline, a touch target
 * tall. A field that [endsWithButton] keeps less inset at its end, since the button's touch target
 * already spaces it from the edge.
 */
internal fun Modifier.inputFrame(endsWithButton: Boolean): Modifier {
    val shape = SmoothCornerShape(SheetTokens.rowRadius)
    return fillMaxWidth()
        .heightIn(min = Sizes.touchTarget)
        .darkSurface(shape)
        .padding(start = SheetTokens.gap, end = if (endsWithButton) Spacing.iconGap else SheetTokens.gap)
}

/** A field's `text2` icon before its text, such as a note's. */
@Composable
internal fun InputIcon(icon: DrawableResource) {
    CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
        Box(Modifier.padding(end = SheetTokens.gap)) { Symbol(icon) }
    }
}

/**
 * A green check when [valid], or a red circled exclamation mark, at a field's end. The mark is drawn only; the
 * field reads its state to screen readers. The palette has no status colors, so it
 * takes the Green and Red folder shades, which reach 3:1 contrast on `bgDark` in every theme.
 */
@Composable
private fun ValidityMark(valid: Boolean) {
    val color = if (valid) FolderColor.Green else FolderColor.Red
    CompositionLocalProvider(LocalContentColor provides (color.tint ?: OtusColors.text2)) {
        Box(Modifier.padding(start = SheetTokens.gap)) {
            Symbol(if (valid) Res.drawable.symbol_check else Res.drawable.symbol_error)
        }
    }
}

/** "Show" or "Hide" at a password field's end, one touch target tall, which says what a tap does. */
@Composable
private fun RevealButton(
    revealed: Boolean,
    onClick: () -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        Modifier
            .heightIn(min = Sizes.touchTarget)
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
            .controlPressFeedback(interactions)
            .padding(horizontal = SheetTokens.gap)
            .semantics { contentDescription = if (revealed) "Hide password" else "Show password" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (revealed) "Hide" else "Show",
            style = panelTextStyle(),
            color = OtusColors.text2,
            fontSize = SheetTokens.detailSize,
        )
    }
}

/**
 * A single-line text field in an [inputFrame], read to screen readers as [description], with
 * [look]'s placeholder and icon, and with [onRemove], a ✕ at its end that removes the field. A
 * password field ends with a button that shows or hides what was typed.
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
    val password = look.kind == InputKind.Password
    // Not saved: a recreated screen hides the password again.
    var revealed by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            modifier.fillMaxWidth().semantics {
                contentDescription = description
                val type = look.kind.contentType
                if (type != null) contentType = type
                when (look.valid) {
                    true -> stateDescription = "Valid"
                    false -> error("Not valid")
                    null -> Unit
                }
            },
        singleLine = true,
        textStyle = style,
        cursorBrush = SolidColor(OtusColors.accentLight),
        visualTransformation =
            if (password && !revealed) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = look.kind.keyboardType),
        decorationBox = { field ->
            Row(Modifier.inputFrame(onRemove != null || password), verticalAlignment = Alignment.CenterVertically) {
                val icon = look.icon
                if (icon != null) InputIcon(icon)
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) Text(look.placeholder, style = style, color = OtusColors.text3, maxLines = 1)
                    field()
                }
                val valid = look.valid
                if (valid != null) ValidityMark(valid)
                if (password) RevealButton(revealed) { revealed = !revealed }
                if (onRemove != null) RemoveFieldButton("Remove $description", onRemove)
            }
        },
    )
}
