package com.otus.designsystem.shared.controls.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.content.ReadTokens
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * The outlined button at the end of a strip or banner, such as Unsubscribe or Show: a `bgRegular`
 * pill [height] tall with a `bgLight` border, an optional [icon], and a semibold label. It keeps a
 * 48 dp touch target. A long label ends in an ellipsis, so the strip's own text keeps room.
 */
@Composable
internal fun StripButton(
    label: String,
    icon: DrawableResource?,
    height: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .sizeIn(minWidth = PanelTokens.touchTarget, minHeight = PanelTokens.touchTarget)
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            Modifier.controlPressFeedback(interactions),
            shape = CircleShape,
            color = OtusColors.bgRegular,
            contentColor = OtusColors.text1,
            border = BorderStroke(PanelTokens.borderWidth, OtusColors.bgLight),
        ) {
            Row(
                Modifier.height(height).padding(horizontal = ReadTokens.stripButtonPadding),
                horizontalArrangement = Arrangement.spacedBy(ReadTokens.glyphGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) Symbol(icon, size = ReadTokens.buttonIconSize)
                Text(
                    label,
                    Modifier.widthIn(max = ReadTokens.stripButtonLabelMaxWidth),
                    style = panelTextStyle(),
                    fontSize = ReadTokens.buttonTextSize,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
