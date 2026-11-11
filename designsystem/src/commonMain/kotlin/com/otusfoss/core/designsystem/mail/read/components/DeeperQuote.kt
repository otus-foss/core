package com.otusfoss.core.designsystem.mail.read.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.content.ReadTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * The first of [levels] quoted inside a [QuoteFold], shown whole under its person and date pill,
 * with the levels it quotes inside it, one step further in. They fold and unfold with the quote
 * around them, never on their own. Nothing shows for no levels.
 */
@Composable
internal fun DeeperQuote(levels: List<QuotedLevel>) {
    val level = levels.firstOrNull() ?: return
    Column(
        Modifier.fillMaxWidth().startBorder().padding(start = ReadTokens.quoteInset),
        verticalArrangement = Arrangement.spacedBy(ReadTokens.quoteHeaderGap),
    ) {
        QuoteHeader(level.person, level.date, level.fullDate, level.onPerson)
        if (level.text.isNotBlank()) {
            Text(
                level.text,
                style = panelTextStyle(),
                color = OtusColors.text3,
                fontSize = ReadTokens.quoteTextSize,
                lineHeight = ReadTokens.quoteLineHeight,
            )
        }
        DeeperQuote(levels.drop(1))
    }
}
