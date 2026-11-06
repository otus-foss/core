package com.otus.designsystem.shared.contacts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.otus.designsystem.shared.input.elements.FieldLabel
import com.otus.designsystem.shared.input.elements.InputBox
import com.otus.designsystem.shared.input.elements.InputIcon
import com.otus.designsystem.shared.input.elements.InputLook
import com.otus.designsystem.shared.input.elements.RemoveFieldButton
import com.otus.designsystem.shared.input.elements.inputFrame
import com.otus.designsystem.theme.tokens.core.Spacing
import com.otus.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * An optional field of the contact editor, such as the note: the [label] above, then the text
 * after its [icon], and a ✕ that removes the field through [onRemove].
 */
@Composable
public fun ContactInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: DrawableResource,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.iconGap)) {
        FieldLabel(label)
        InputBox(value, onValueChange, label, InputLook(icon = icon), onRemove = onRemove)
    }
}

/**
 * A contact editor field whose value is picked rather than typed, such as the birthday: the
 * [label] above, the [value] after its [icon] in a field that opens the picker through
 * [onClick], and a ✕ that removes it through [onRemove].
 */
@Composable
public fun ContactPickerField(
    label: String,
    value: String,
    icon: DrawableResource,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.iconGap)) {
        FieldLabel(label)
        Row(
            Modifier.clickable(role = Role.Button, onClick = onClick).inputFrame(endsWithButton = true),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InputIcon(icon)
            Text(value, Modifier.weight(1f), style = panelTextStyle(), maxLines = 1)
            RemoveFieldButton("Remove $label", onRemove)
        }
    }
}
