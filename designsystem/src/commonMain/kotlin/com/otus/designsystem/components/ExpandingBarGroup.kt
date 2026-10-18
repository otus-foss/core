package com.otus.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_more_horiz
import com.otus.designsystem.theme.ActionBarTokens
import com.otus.designsystem.theme.MenuTokens
import com.otus.designsystem.theme.MotionCurve
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.motionTween

/**
 * A bar's group: [content] in a row with ⋯ at its end, named [moreLabel]. ⋯ grows the group
 * upward into one surface, [panel] above a divider and the row, such as an image's options above
 * its sizes, with ⋯ filled while it is open. ⋯ again, a tap outside, Back, or the close [panel]
 * gets folds it back. Inside a [BarMorph], the folded group is the bar's group part.
 */
@Composable
public fun ExpandingBarGroup(
    moreLabel: String,
    panel: @Composable (close: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val shown = remember { MutableTransitionState(false) }
    var width by remember { mutableIntStateOf(0) }
    val close = { shown.targetState = false }
    Box(modifier.onSizeChanged { width = it.width }.groupSurface()) {
        GroupRow(open = false, moreLabel, onMore = { shown.targetState = true }, content)
        // Drawn over the folded group, bottom to bottom, so the group seems to grow upward.
        if (shown.currentState || shown.targetState) {
            Popup(OverAnchorBottom, onDismissRequest = close, properties = PopupProperties(focusable = true)) {
                val density = LocalDensity.current
                Column(Modifier.width(with(density) { width.toDp() }).groupSurface()) {
                    AnimatedVisibility(
                        shown,
                        enter =
                            expandVertically(motionTween(MotionSpeed.Fast, MotionCurve.Enter), Alignment.Bottom) +
                                fadeIn(motionTween(MotionSpeed.Fast)),
                        exit =
                            shrinkVertically(motionTween(MotionSpeed.Fast, MotionCurve.Exit), Alignment.Bottom) +
                                fadeOut(motionTween(MotionSpeed.Fast)),
                    ) {
                        Column(Modifier.padding(top = MenuTokens.inset)) {
                            panel(close)
                            OptionsDivider()
                        }
                    }
                    GroupRow(open = true, moreLabel, onMore = close, content)
                }
            }
        }
    }
}

/** The row of a group, at the bar's height, with ⋯ at its end; ⋯ is filled while [open]. */
@Composable
private fun GroupRow(
    open: Boolean,
    moreLabel: String,
    onMore: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().height(ActionBarTokens.height).padding(start = ActionBarTokens.groupPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
        Box(
            Modifier
                .size(PanelTokens.touchTarget)
                .semantics {
                    contentDescription = moreLabel
                    selected = open
                }.clickable(role = Role.Button, onClick = onMore),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(
                        PanelTokens.chipHeight,
                    ).background(if (open) OtusColors.bgLight else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Symbol(Res.drawable.symbol_more_horiz) }
        }
    }
}

/** The group's `bgDark` surface with its outline, as an [ActionBar]'s group has. */
private fun Modifier.groupSurface(): Modifier {
    val shape = RoundedCornerShape(ActionBarTokens.radius)
    return background(OtusColors.bgDark, shape).border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape)
}

/** Puts the popup's bottom on the anchor's bottom, at its start, so it grows upward from it. */
private object OverAnchorBottom : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset(anchorBounds.left, anchorBounds.bottom - popupContentSize.height)
}
