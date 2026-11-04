package com.otus.designsystem.shared.contacts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.optionalButton
import com.otus.designsystem.shared.settings.elements.ValueButton
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.tokens.people.ContactTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * One value of a contact in a raised [RowGroup][com.otus.designsystem.shared.rows.components.RowGroup]:
 * an optional `text2` [icon], the [value] over a `text2` [caption] such as "Primary", then a
 * [label] in a value chip such as "Work" on the right. A [highlighted] row fills with
 * `selectedFill`, such as a contact's primary address among the others, and its chip takes a
 * `text3` outline to stand out on it. [onClick] acts on the value, as writing to an address does.
 */
@Composable
public fun ContactValueRow(
    value: String,
    caption: String?,
    modifier: Modifier = Modifier,
    icon: DrawableResource? = null,
    label: String? = null,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .highlightedValue(highlighted)
            .heightIn(min = ContactTokens.valueRowHeight)
            .optionalButton(onClick)
            .padding(horizontal = SheetTokens.inset, vertical = SheetTokens.rowPadding),
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) { Symbol(icon) }
        }
        Column(Modifier.weight(1f)) {
            Text(value, style = panelTextStyle())
            if (caption != null) {
                Text(caption, style = panelTextStyle(), color = OtusColors.text2, fontSize = ContactTokens.detailSize)
            }
        }
        if (label != null) ValueButton(label, outline = if (highlighted) OtusColors.text3 else OtusColors.bgLight)
    }
}

private fun Modifier.highlightedValue(highlighted: Boolean): Modifier =
    if (highlighted) background(OtusColors.selectedFill) else this
