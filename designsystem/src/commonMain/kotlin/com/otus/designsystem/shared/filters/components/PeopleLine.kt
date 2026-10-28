package com.otus.designsystem.shared.filters.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.shared.people.components.Recipient
import com.otus.designsystem.shared.people.components.RemovablePersonChip
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.tokens.selection.FilterTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * One line of the People card in a filter panel, such as From: the `text3` [label] in its column,
 * a [RemovablePersonChip] for each of [people], wrapping as needed, and the `text3` [placeholder], such as
 * "Add people…", in the rest of the line. A tap anywhere on the line except a chip's ✕ calls
 * [onOpen]; a chip's ✕ calls [onRemove] with its index. Screen readers hear the line as one
 * button, such as "From: Grace Hopper, ChatGPT. Add people…", and each ✕ on its own.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun PeopleLine(
    label: String,
    people: List<Recipient>,
    placeholder: String,
    onOpen: () -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spoken = listOfNotNull(people.joinToString { it.name }.ifEmpty { null }, placeholder).joinToString(". ")
    Row(
        modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onOpen)
            .semantics { contentDescription = "$label: $spoken" }
            .padding(horizontal = SheetTokens.inset, vertical = FilterTokens.chipGap),
        verticalAlignment = Alignment.Top,
    ) {
        // At least the column's width, and wider with large text, so the label never runs into the chips.
        LineSlot(Modifier.widthIn(min = FilterTokens.peopleLabelWidth).padding(end = FilterTokens.chipGap)) {
            Text(label, style = panelTextStyle(), color = OtusColors.text3, fontSize = FilterTokens.chipTextSize)
        }
        FlowRow(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(FilterTokens.chipGap),
            verticalArrangement = Arrangement.spacedBy(FilterTokens.chipGap),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            people.forEachIndexed { index, person -> RemovablePersonChip(person, { onRemove(index) }) }
            LineSlot(Modifier.weight(1f).widthIn(min = FilterTokens.placeholderMinWidth)) {
                Text(
                    placeholder,
                    style = panelTextStyle(),
                    color = OtusColors.text3,
                    fontSize = FilterTokens.chipTextSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** A chip-tall box that centers its text on the first row of chips; its text is read with the line. */
@Composable
private fun LineSlot(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier.heightIn(min = FilterTokens.chipHeight).clearAndSetSemantics {},
        contentAlignment = Alignment.CenterStart,
    ) { content() }
}
