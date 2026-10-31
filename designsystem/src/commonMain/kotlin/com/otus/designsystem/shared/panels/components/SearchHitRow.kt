package com.otus.designsystem.shared.panels.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_mail
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.typography.panelTextStyle

/** One message a search found, as a mail symbol, its [title], and its [detail], such as the date, at the end. */
@Composable
public fun SearchHitRow(
    title: String,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = PanelTokens.searchHitHeight)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = PanelTokens.panelInset),
        horizontalArrangement = Arrangement.spacedBy(PanelTokens.searchHitGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides OtusColors.text2) { Symbol(Res.drawable.symbol_mail) }
        Text(title, Modifier.weight(1f), style = panelTextStyle(), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(detail, style = panelTextStyle(), color = OtusColors.text2, fontSize = PanelTokens.countSize, maxLines = 1)
    }
}
