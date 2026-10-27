package com.otus.designsystem.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SidebarTokens
import com.otus.designsystem.theme.panelTextStyle

/** A mailbox's count in an `accentDark` pill, ringed in [surface], the fill of the tile or row it sits on. */
@Composable
internal fun SidebarBadge(
    text: String,
    surface: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .heightIn(min = SidebarTokens.badgeHeight)
            .widthIn(min = SidebarTokens.badgeHeight)
            .ringedFill(surface, OtusColors.accentDark)
            .padding(horizontal = SidebarTokens.badgePadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = panelTextStyle(),
            fontSize = SidebarTokens.badgeSize,
            lineHeight = SidebarTokens.badgeSize,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

/**
 * A pill in [ring], with [fill] inset by the ring's width. The fill never reaches the pill's edge,
 * so no sliver of it shows past the ring's anti-aliased outline, and the pill keeps its size.
 */
private fun Modifier.ringedFill(
    ring: Color,
    fill: Color,
): Modifier =
    drawBehind {
        val inset = SidebarTokens.badgeRing.toPx()
        val radius = size.height / 2
        drawRoundRect(ring, cornerRadius = CornerRadius(radius))
        drawRoundRect(
            fill,
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            cornerRadius = CornerRadius(radius - inset),
        )
    }
