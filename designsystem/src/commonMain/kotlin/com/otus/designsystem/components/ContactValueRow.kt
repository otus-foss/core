package com.otus.designsystem.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.semantics.Role
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.elements.ValueButton
import com.otus.designsystem.theme.ContactTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * One value of a contact in a raised [RowGroup]: an optional `text2` [icon], the [value] over a
 * `text2` [caption] such as "Birthday", then a [label] in a value chip such as "Work" on the
 * right. In a [highlighted] RowGroup the chip takes an `accentLight` outline to stand out on the
 * accent fill. [onClick] acts on the value, as writing to an address does, and [onLongClick]
 * offers more, such as copying it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ContactValueRow(
    value: String,
    caption: String?,
    modifier: Modifier = Modifier,
    icon: DrawableResource? = null,
    label: String? = null,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val clickable =
        if (onClick == null && onLongClick == null) {
            Modifier
        } else {
            Modifier.combinedClickable(role = Role.Button, onLongClick = onLongClick, onClick = onClick ?: {})
        }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = ContactTokens.valueRowHeight)
            .then(clickable)
            .padding(horizontal = SheetTokens.inset, vertical = SheetTokens.rowPadding),
        horizontalArrangement = Arrangement.spacedBy(SheetTokens.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) { Symbol(it) }
        }
        Column(Modifier.weight(1f)) {
            Text(value, style = panelTextStyle())
            caption?.let {
                Text(it, style = panelTextStyle(), color = OtusColors.text2, fontSize = ContactTokens.detailSize)
            }
        }
        label?.let { ValueButton(it, outline = if (highlighted) OtusColors.accentLight else OtusColors.bgLight) }
    }
}
