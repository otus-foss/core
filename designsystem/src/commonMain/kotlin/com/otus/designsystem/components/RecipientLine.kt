package com.otus.designsystem.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.ComposeLine
import com.otus.designsystem.theme.ComposeTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A line of people a message goes to, such as To: [label] in its column, then a chip for each of
 * [recipients], wrapping as needed, and the [input] field for the next address. Tapping a chip runs
 * [onChipClick] with its index.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun RecipientLine(
    label: String,
    recipients: List<Recipient>,
    input: AddressInput,
    onChipClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    ComposeLine(label, modifier) {
        FlowRow(
            Modifier.weight(1f).padding(vertical = PanelTokens.iconGap),
            horizontalArrangement = Arrangement.spacedBy(ComposeTokens.recipientGap),
            verticalArrangement = Arrangement.spacedBy(ComposeTokens.recipientGap),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            recipients.forEachIndexed { index, recipient -> RecipientChip(recipient, { onChipClick(index) }) }
            AddressField(label, input, Modifier.weight(1f))
        }
    }
}

/** A person's avatar and name in a small outlined box; tapping it edits or removes them. */
@Composable
public fun RecipientChip(
    recipient: Recipient,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(ComposeTokens.recipientRadius)
    Row(
        modifier
            .height(ComposeTokens.recipientHeight)
            .border(PanelTokens.borderWidth, OtusColors.bgLight, shape)
            .clickable(onClickLabel = "Edit", role = Role.Button, onClick = onClick)
            .semantics { contentDescription = recipient.description }
            .padding(start = PanelTokens.iconGap, end = PanelTokens.chipPadding),
        horizontalArrangement = Arrangement.spacedBy(PanelTokens.chipGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PersonAvatar(recipient.avatar, ComposeTokens.recipientAvatarSize)
        Text(recipient.name, style = panelTextStyle(), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AddressField(
    label: String,
    input: AddressInput,
    modifier: Modifier,
) {
    val text = input.text
    BasicTextField(
        value = text,
        onValueChange = input.onTextChange,
        modifier =
            modifier
                .widthIn(min = ComposeTokens.labelWidth)
                .heightIn(min = ComposeTokens.recipientHeight)
                .semantics { contentDescription = label }
                .onFocusChanged { if (!it.isFocused) input.onCommit() }
                .onPreviewKeyEvent { event ->
                    val backspace = event.key == Key.Backspace && event.type == KeyEventType.KeyDown
                    (backspace && text.isEmpty()).also { if (it) input.onRemoveLast() }
                },
        singleLine = true,
        textStyle = panelTextStyle(),
        cursorBrush = SolidColor(OtusColors.accentLight),
        keyboardOptions =
            KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        keyboardActions =
            KeyboardActions(
                onNext = { if (text.isBlank()) defaultKeyboardAction(ImeAction.Next) else input.onCommit() },
            ),
        decorationBox = { field -> Row(verticalAlignment = Alignment.CenterVertically) { field() } },
    )
}
