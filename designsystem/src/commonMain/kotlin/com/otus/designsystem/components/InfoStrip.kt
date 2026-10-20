package com.otus.designsystem.components

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
import com.otus.designsystem.elements.StripButton
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.ReadTokens
import com.otus.designsystem.theme.panelTextStyle
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
    action: StripAction?,
    modifier: Modifier = Modifier,
) {
    StripCard(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = ReadTokens.positionHeight)
                .padding(start = ReadTokens.stripStart + ReadTokens.stripGap / 2, end = ReadTokens.stripEnd),
            horizontalArrangement = Arrangement.spacedBy(ReadTokens.stripGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                Symbol(icon, size = ReadTokens.infoIconSize)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = panelTextStyle(),
                    fontSize = ReadTokens.titleSize,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.let {
                    Text(
                        it,
                        style = panelTextStyle(),
                        color = OtusColors.text3,
                        fontSize = ReadTokens.detailSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            action?.let { StripButton(it.label, it.icon, ReadTokens.stripButtonHeight, it.onClick) }
        }
    }
}
