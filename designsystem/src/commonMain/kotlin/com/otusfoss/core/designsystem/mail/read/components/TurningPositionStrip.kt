package com.otusfoss.core.designsystem.mail.read.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.otusfoss.core.designsystem.shared.pages.elements.PageDots
import com.otusfoss.core.designsystem.shared.pages.elements.TurnProgress
import com.otusfoss.core.designsystem.shared.pages.elements.shown
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.shapes.strokeBorder
import com.otusfoss.core.designsystem.theme.tokens.content.ReadTokens
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes

/**
 * A [PositionStrip] for pages that turn under a finger. It keeps still during the drag: the dot of
 * the page the drag heads to swells with [turn], and once letting go would turn the page the strip
 * shows that page instead of the one at [current]. [page] gives each page's content, and
 * [trailing] shows on every page. Screen readers hear the page the strip shows.
 */
@Composable
public fun TurningPositionStrip(
    count: Int,
    current: Int,
    turn: TurnProgress?,
    page: (Int) -> PositionPage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    // The strip shows a page that exists, even if a caller's index runs past the ends.
    val shown = turn.shown(current).coerceIn(0, (count - 1).coerceAtLeast(0))
    val content = page(shown)
    val position = StripPosition(count, shown, content.detail, turn)
    PositionCard(content.description ?: position.spoken(content.heading), onClick, modifier) {
        PositionInfo(content.heading, position, Modifier.weight(1f), trailing)
    }
}

/**
 * The dots of a [TurningPositionStrip] on their own, in a small `bgDark` pill, for a screen that
 * hides its strips: the page at [current] of [count] and the drag toward another, [turn].
 */
@Composable
public fun TurnDotsPill(
    count: Int,
    current: Int,
    turn: TurnProgress,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .height(ReadTokens.turnPillHeight)
            .clip(CircleShape)
            .background(OtusColors.bgDark)
            .strokeBorder(Sizes.borderWidth, OtusColors.strokeBgDark, CircleShape)
            .padding(horizontal = ReadTokens.turnPillInset),
        contentAlignment = Alignment.Center,
    ) {
        PageDots(count, current, turn = turn)
    }
}
