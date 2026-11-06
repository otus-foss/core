package com.otus.designsystem.shared.feedback.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otus.designsystem.shared.controls.elements.StripButton
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.controls.elements.UiAction
import com.otus.designsystem.shared.rows.components.StripCard
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.content.ReadTokens
import com.otus.designsystem.theme.tokens.rows.StripTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A strip that says what a message is and offers one step, such as a newsletter's name and
 * "Mailing list · about once a week" with Unsubscribe. The same strip confirms in place after the
 * step, with new text and the next [action].
 */
@Composable
public fun InfoStrip(
    icon: DrawableResource,
    title: String,
    subtitle: String?,
    action: UiAction?,
    modifier: Modifier = Modifier,
) {
    StripCard(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = StripTokens.height)
                .padding(start = StripTokens.start + StripTokens.gap / 2, end = StripTokens.end),
            horizontalArrangement = Arrangement.spacedBy(StripTokens.gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                Symbol(icon, size = StripTokens.iconSize)
            }
            StripText(title, subtitle, Modifier.weight(1f))
            if (action != null) StripButton(action.label, action.symbol, StripTokens.buttonHeight, action.onClick)
        }
    }
}

@Composable
private fun StripText(
    title: String,
    subtitle: String?,
    modifier: Modifier,
) {
    Column(modifier) {
        Text(
            title,
            style = panelTextStyle(),
            fontSize = StripTokens.titleSize,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
            Text(
                subtitle,
                style = panelTextStyle(),
                color = OtusColors.text3,
                fontSize = ReadTokens.detailSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
