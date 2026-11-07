package com.otus.designsystem.shared.actionbar.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_more_horiz
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.overlays.components.OptionsDivider
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.shapes.darkSurface
import com.otus.designsystem.theme.tokens.core.Sizes
import com.otus.designsystem.theme.tokens.overlays.ActionBarTokens
import com.otus.designsystem.theme.tokens.overlays.MenuTokens

/**
 * A bar's group: [content] in a row with ⋯ at its end, named [moreLabel]. ⋯ grows the group
 * itself upward, [panel] above a divider and the row, such as an image's options above its sizes,
 * with ⋯ filled while it is open. ⋯ again, Back, or the close [panel] gets folds it back. Inside
 * a [BarMorph], the group is the bar's group part.
 */
@Composable
internal fun ExpandingBarGroup(
    moreLabel: String,
    panel: @Composable (close: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    val close = { open = false }
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = open,
        onBackCompleted = close,
    )
    // The bar sits at the bottom, so growing the group taller grows it upward.
    Column(modifier.darkSurface(SmoothCornerShape(ActionBarTokens.radius))) {
        AnimatedVisibility(
            open,
            enter =
                expandVertically(motionTween(MotionSpeed.Normal, MotionCurve.Enter), Alignment.Bottom) +
                    fadeIn(motionTween(MotionSpeed.Normal)),
            exit =
                shrinkVertically(motionTween(MotionSpeed.Normal, MotionCurve.Exit), Alignment.Bottom) +
                    fadeOut(motionTween(MotionSpeed.Fast)),
        ) {
            Column(Modifier.padding(top = MenuTokens.inset)) {
                panel(close)
                OptionsDivider()
            }
        }
        GroupRow(open, moreLabel, onMore = { open = !open }, content)
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
                .size(Sizes.touchTarget)
                .semantics {
                    contentDescription = moreLabel
                    selected = open
                }.clickable(role = Role.Button, onClick = onMore),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(Sizes.chipHeight)
                    .background(if (open) OtusColors.bgLight else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Symbol(Res.drawable.symbol_more_horiz) }
        }
    }
}
