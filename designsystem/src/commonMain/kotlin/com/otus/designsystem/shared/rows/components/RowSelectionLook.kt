package com.otus.designsystem.shared.rows.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_check
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.InboxTokens
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
