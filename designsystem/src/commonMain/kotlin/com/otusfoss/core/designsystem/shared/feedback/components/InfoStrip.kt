package com.otusfoss.core.designsystem.shared.feedback.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.otusfoss.core.designsystem.shared.controls.elements.StripButton
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import com.otusfoss.core.designsystem.shared.rows.components.StripCard
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.motion.motionTween
import com.otusfoss.core.designsystem.theme.tokens.rows.StripTokens
import com.otusfoss.core.designsystem.theme.typography.TypeScale
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A strip that says what a message is and offers one step, such as a newsletter's name and
 * "Mailing list · about once a week" with Unsubscribe. The same strip confirms in place after the
 * step, with new text and the next [action]. Without [framed], the card, symbol and text fade
 * away and only the button stays, in the same place, until the strip is gone.
 */
@Composable
public fun InfoStrip(
    icon: DrawableResource,
    title: String,
    subtitle: String?,
    action: UiAction?,
    modifier: Modifier = Modifier,
    framed: Boolean = true,
) {
    val frame by animateFloatAsState(if (framed) 1f else 0f, motionTween())
    StripCard(modifier, frame) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = StripTokens.height)
                .padding(start = StripTokens.start + StripTokens.gap / 2, end = StripTokens.end),
            horizontalArrangement = Arrangement.spacedBy(StripTokens.gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
                Box(Modifier.alpha(frame)) { Symbol(icon, size = StripTokens.iconSize) }
            }
            // Hidden text leaves the layout only once faded, so screen readers stop at the button.
            if (frame > 0f) {
                StripText(title, subtitle, Modifier.weight(1f).alpha(frame))
            } else {
                Spacer(Modifier.weight(1f))
            }
            if (action != null) {
                StripButton(
                    action.label,
                    action.symbol,
                    StripTokens.buttonHeight,
                    action.onClick,
                    minWidth = StripTokens.buttonMinWidth,
                )
            }
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
                fontSize = TypeScale.detail,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
