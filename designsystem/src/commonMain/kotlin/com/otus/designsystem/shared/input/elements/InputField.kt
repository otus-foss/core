package com.otus.designsystem.shared.input.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.core.Spacing
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/** Labelled single-line form input. */
@Composable
public fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    supportingText: String? = null,
    kind: InputKind = InputKind.Text,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.iconGap)) {
        FieldLabel(label)
        InputBox(value, onValueChange, label, InputLook(placeholder, kind))
        if (supportingText != null) {
            Text(
                supportingText,
                Modifier.padding(horizontal = SheetTokens.gap),
                style = panelTextStyle(),
                fontSize = SheetTokens.detailSize,
                color = OtusColors.text2,
            )
        }
    }
}
