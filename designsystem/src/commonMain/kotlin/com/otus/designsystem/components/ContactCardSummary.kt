package com.otus.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.theme.ContactTokens
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * One contact card as it stands, such as a source of a merge: its [name], the [book] it is saved
 * in with the book's [bookIcon], each of its [addresses] as a person pill so a long address stays
 * on one line, and `text2` [details] such as a phone number.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun ContactCardSummary(
    name: String,
    book: String,
    bookIcon: DrawableResource,
    addresses: List<Recipient>,
    details: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(OtusColors.bgRegular, RoundedCornerShape(SheetTokens.rowRadius))
            .padding(SheetTokens.gap),
        verticalArrangement = Arrangement.spacedBy(ContactTokens.summaryGap),
    ) {
        // The name and the book each get a line of their own, so a long one never squeezes the other.
        Text(
            name,
            style = panelTextStyle(),
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                Symbol(bookIcon, size = PanelTokens.iconSize * 0.8f)
            }
            Text(
                book,
                Modifier.padding(start = PanelTokens.iconGap),
                style = panelTextStyle(),
                color = OtusColors.text2,
                fontSize = ContactTokens.detailSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(PanelTokens.chipGap),
            verticalArrangement = Arrangement.spacedBy(PanelTokens.chipGap),
        ) {
            addresses.forEach { PersonChip(it) }
        }
        details?.let {
            Text(it, style = panelTextStyle(), color = OtusColors.text2, fontSize = ContactTokens.detailSize)
        }
    }
}
