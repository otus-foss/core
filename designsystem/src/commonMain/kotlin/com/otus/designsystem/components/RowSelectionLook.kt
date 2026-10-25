package com.otus.designsystem.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_check
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.InboxTokens
import com.otus.designsystem.theme.OtusAlpha
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.motionTween

/**
 * The tint behind a selected row. Corners square off where [RowSelection.joinedAbove] or
 * [RowSelection.joinedBelow] says a selected neighbor touches, so the tints merge into one block.
 */
@Composable
internal fun Modifier.selectionTint(selection: RowSelection?): Modifier {
    val top = selectionCorner(selection?.joinedAbove == true)
    val bottom = selectionCorner(selection?.joinedBelow == true)
    val tint =
        animateColorAsState(
            if (selection?.selected == true) OtusColors.accentDark else Color.Transparent,
            motionTween(),
        ).value
    return clip(RoundedCornerShape(top, top, bottom, bottom)).background(tint)
}

/** [leading], or a check circle [markSize] wide while the row is [selected]. */
@Composable
internal fun SelectableLeading(
    selected: Boolean,
    markSize: Dp,
    leading: @Composable () -> Unit,
) {
    Crossfade(selected, animationSpec = motionTween()) { if (it) SelectedMark(markSize) else leading() }
}

/**
 * A card on the theme's raised color behind a list row, filled with the selection tint while the
 * row is selected. A [dimmed] row's card fades toward the page as much as its text fades, so read
 * and unread rows differ in both. The gaps between cards keep selected neighbors apart, so corners
 * never join. On a theme whose raised color is its page color, such as Black, an outline marks the card.
 */
@Composable
internal fun Modifier.rowCard(
    selection: RowSelection?,
    dimmed: Boolean,
): Modifier {
    val selected = selection?.selected == true
    val strength = if (dimmed) OtusAlpha.DIMMED else 1f
    val raised = lerp(OtusColors.bgRegular, OtusColors.bgDark, strength)
    val fill = animateColorAsState(if (selected) OtusColors.accentDark else raised, motionTween()).value
    val shape = RoundedCornerShape(InboxTokens.selectionRadius)
    val outline = OtusColors.strokeBgDark.copy(alpha = strength)
    val outlined = !selected && OtusColors.bgDark == OtusColors.bgRegular
    // Drawn as a shape rather than inside a clip layer, and clipped afterwards for the press ripple.
    return background(fill, shape)
        .then(if (outlined) Modifier.border(InboxTokens.cardOutline, outline, shape) else Modifier)
        .clip(shape)
}

@Composable
private fun selectionCorner(joined: Boolean): Dp =
    animateDpAsState(if (joined) 0.dp else InboxTokens.selectionRadius, motionTween()).value

@Composable
private fun SelectedMark(size: Dp) {
    Box(
        Modifier.size(size).background(OtusColors.accentRegular, CircleShape),
        contentAlignment = Alignment.Center,
    ) { Symbol(Res.drawable.symbol_check, size = ActionBarTokens.symbolSize) }
}
