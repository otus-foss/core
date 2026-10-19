package com.otus.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.otus.designsystem.elements.ExpandChevron
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.ReadTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A level quoted inside a [QuoteFold], folded to its header row: the person and date pill, then a
 * chevron. A tap on the row shows its own text under it, and another folds it again.
 */
@Composable
internal fun DeeperQuote(level: QuotedLevel) {
    var open by rememberSaveable(level.text) { mutableStateOf(false) }
    Column(
        Modifier.fillMaxWidth().startBorder().padding(start = ReadTokens.quoteInset),
        verticalArrangement = Arrangement.spacedBy(ReadTokens.quoteHeaderGap),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = PanelTokens.touchTarget)
                .clickable(onClickLabel = if (open) "Fold" else "Show", role = Role.Button) { open = !open },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuoteHeader(level.person, level.date, level.onPerson)
            Spacer(Modifier.weight(1f))
            ExpandChevron(open)
        }
        if (open && level.text.isNotBlank()) {
            Text(
                level.text,
                style = panelTextStyle(),
                color = OtusColors.text3,
                fontSize = ReadTokens.quoteTextSize,
                lineHeight = ReadTokens.quoteLineHeight,
            )
        }
    }
}
