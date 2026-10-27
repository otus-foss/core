package com.otus.designsystem.mail.compose.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.otus.designsystem.mail.compose.elements.ComposeLine
import com.otus.designsystem.shared.people.components.Recipient
import com.otus.designsystem.shared.people.components.RecipientChip
import com.otus.designsystem.theme.ComposeTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A line of people a message goes to, such as To: [label] in its column, then a chip for each of
 * [recipients], wrapping as needed, and the [input] field for the next address. Tapping a chip runs
 * [onChipClick] with its index. Past [collapsedAfter] people, only that many show, then a
 * "+3 more" chip that shows the rest.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun RecipientLine(
    label: String,
    recipients: List<Recipient>,
    input: AddressInput,
    onChipClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    collapsedAfter: Int = Int.MAX_VALUE,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    // One more person fits in place of a "+1 more" chip. Subtracting keeps Int.MAX_VALUE from overflowing.
    val folded = !expanded && recipients.size - 1 > collapsedAfter
    val shown = if (folded) recipients.take(collapsedAfter) else recipients
    ComposeLine(label, modifier, topAligned = true) {
        FlowRow(
            // Centers a single row of chips in the 48 dp line, beside the label.
            Modifier.weight(1f).padding(vertical = (PanelTokens.touchTarget - ComposeTokens.recipientHeight) / 2),
            horizontalArrangement = Arrangement.spacedBy(ComposeTokens.recipientGap),
            verticalArrangement = Arrangement.spacedBy(ComposeTokens.recipientGap),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            shown.forEachIndexed { index, recipient -> RecipientChip(recipient, { onChipClick(index) }) }
            // A folded line shows no field: "+3 more" opens it, so the line keeps to its chips.
            if (folded) {
                MoreChip(recipients.size - shown.size) {
                    expanded = true
                }
            } else {
                AddressField(label, input, Modifier.weight(1f))
            }
        }
    }
}

/** The count of people a folded line hides, such as "+3 more"; tapping it shows them. */
@Composable
private fun MoreChip(
    hidden: Int,
    onClick: () -> Unit,
) {
    val shape = CircleShape
    Box(
        Modifier
            .height(ComposeTokens.recipientHeight)
            .border(PanelTokens.borderWidth, OtusColors.bgLight, shape)
            .clickable(onClickLabel = "Show everyone", role = Role.Button, onClick = onClick)
            .padding(horizontal = PanelTokens.chipPadding),
        contentAlignment = Alignment.Center,
    ) { Text("+$hidden more", style = panelTextStyle(), color = OtusColors.text2, fontSize = ComposeTokens.detailSize) }
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
