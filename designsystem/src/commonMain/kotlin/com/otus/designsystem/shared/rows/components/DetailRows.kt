package com.otus.designsystem.shared.rows.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.rows.DetailsTableTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/**
 * Label and value rows, such as a message's sender details: From, To, Date, and Folders. The labels
 * line up in a narrow `text3` column that grows with the font scale and wraps a long label to a
 * second line, and hairlines separate the rows. They draw no background, so they sit straight on a
 * panel.
 */
@Composable
public fun DetailRows(
    rows: List<DetailLine>,
    modifier: Modifier = Modifier,
) {
    val labelWidth = DetailsTableTokens.labelWidth * LocalDensity.current.fontScale
    Column(modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, row ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .topHairline(OtusColors.strokeBgDark) { index > 0 }
                    .padding(vertical = DetailsTableTokens.padding),
                horizontalArrangement = Arrangement.spacedBy(DetailsTableTokens.padding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    row.label,
                    Modifier.width(labelWidth),
                    style = panelTextStyle(),
                    color = OtusColors.text3,
                    fontSize = DetailsTableTokens.labelSize,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(Modifier.weight(1f)) { row.content() }
            }
        }
    }
}
