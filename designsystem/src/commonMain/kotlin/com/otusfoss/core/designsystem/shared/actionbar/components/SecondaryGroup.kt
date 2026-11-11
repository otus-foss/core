package com.otusfoss.core.designsystem.shared.actionbar.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import com.otusfoss.core.designsystem.shared.actionbar.elements.BarAddButton
import com.otusfoss.core.designsystem.shared.actionbar.elements.BarIconButton
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import com.otusfoss.core.designsystem.theme.motion.MotionSpeed
import com.otusfoss.core.designsystem.theme.motion.motionTween
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.shapes.darkSurface
import com.otusfoss.core.designsystem.theme.tokens.overlays.ActionBarTokens

/**
 * The bar's secondary [actions] in one surface. A button with a menu grows the group into it, and
 * a drag up anywhere on the group grows it into [allActions]; see [GroupMenu]. The open menu
 * draws the group's icons again, so the group's own hide at once.
 */
@Composable
internal fun SecondaryGroup(
    actions: List<BarAction>,
    onAdd: (() -> Unit)?,
    allActions: List<UiAction>,
    modifier: Modifier,
) {
    val shape = SmoothCornerShape(ActionBarTokens.radius)
    val menu = rememberGroupMenu()
    val hidden by animateFloatAsState(
        if (menu.actions.isEmpty()) 1f else 0f,
        motionTween(MotionSpeed.Quick),
        label = "Group icons",
    )
    // The bar sets the group's width; while it moves in, the icons keep their places and clip.
    Box(
        modifier
            .height(ActionBarTokens.height)
            .onSizeChanged { menu.group = it }
            .pullsUp(menu, allActions)
            .clip(shape)
            .darkSurface(shape),
    ) {
        GroupIcons(actions, onAdd, Modifier.align(Alignment.CenterStart).graphicsLayer { alpha = hidden }) {
            if (it.menu.isEmpty()) it.action.onClick() else menu.open(it.menu)
        }
        menu.Shown {
            // A tap on the open menu's group closes it first, and runs the action if it has no menu.
            val add = onAdd?.let { run -> { menu.close().also { run() } } }
            GroupIcons(actions, add, Modifier.align(Alignment.CenterStart)) {
                menu.close()
                if (it.menu.isEmpty()) it.action.onClick()
            }
        }
    }
}

@Composable
private fun GroupIcons(
    actions: List<BarAction>,
    onAdd: (() -> Unit)?,
    modifier: Modifier,
    onClick: (BarAction) -> Unit,
) {
    Row(
        modifier
            .wrapContentWidth(Alignment.Start, unbounded = true)
            .padding(horizontal = ActionBarTokens.groupPadding),
        horizontalArrangement = Arrangement.spacedBy(ActionBarTokens.slotGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        actions.forEach {
            BarIconButton(it.action.label, it.symbol, { onClick(it) }, enabled = it.action.enabled)
        }
        if (onAdd != null) BarAddButton("Add action", onAdd)
    }
}
