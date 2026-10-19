package com.otus.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.ReadTokens
import com.otus.designsystem.theme.panelTextStyle

/**
 * A level quoted inside a [QuoteFold], shown whole under its person and date pill. It folds and
 * unfolds with the quote around it, never on its own.
 */
@Composable
internal fun DeeperQuote(level: QuotedLevel) {
    Column(
        Modifier.fillMaxWidth().startBorder().padding(start = ReadTokens.quoteInset),
        verticalArrangement = Arrangement.spacedBy(ReadTokens.quoteHeaderGap),
    ) {
        QuoteHeader(level.person, level.date, level.onPerson)
        if (level.text.isNotBlank()) {
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
