package com.otus.designsystem.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * The borderless text of a message being written, below its [ComposeField]s. The whole area
 * the caller gives it, such as the rest of the page through a weight, takes taps and grows with
 * the text. [placeholder] shows in `text3` while it is empty and names it to accessibility
 * services.
 */
@Composable
public fun ComposeBody(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val style = panelTextStyle()
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().semantics { contentDescription = placeholder },
        textStyle = style,
        cursorBrush = SolidColor(OtusColors.accentLight),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        decorationBox = { field ->
            Box(Modifier.padding(horizontal = PageTokens.textInset, vertical = PageTokens.contentInset)) {
                if (value.isEmpty()) Text(placeholder, style = style, color = OtusColors.text3)
                field()
            }
        },
    )
}
