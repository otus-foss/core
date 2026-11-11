package com.otusfoss.core.designsystem.shared.controls.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.motion.MotionCurve
import com.otusfoss.core.designsystem.theme.motion.MotionSpeed
import com.otusfoss.core.designsystem.theme.motion.motionTween
import com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * A row of [options] in one track, one of them [selected] with a `selectedFill` pill, such as
 * Copy from and Copy to; none while [selected] is -1. The pill slides to a new selection, easing
 * in and out at the user's animation speed. Each label stays on one line at [fontSize],
 * in a segment as wide as its share of the labels' length, so a longer label gets more room.
 * [height] is the track's, such as
 * [com.otusfoss.core.designsystem.theme.tokens.controls.ControlTokens.segmentHeight] in a filter panel.
 * [track] fills the track and the unselected segments: the page color on a raised panel, or the
 * raised color on a page, so the track always shows.
 */
@Composable
public fun SegmentedToggle(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    height: Dp = ControlTokens.toggleHeight,
    track: Color = OtusColors.bgRegular,
) {
    val shares = options.map { it.length.coerceAtLeast(1).toFloat() }
    val edges = shares.runningFold(0f, Float::plus).map { it / shares.sum() }
    val slide = motionTween<Float>(MotionSpeed.Normal, MotionCurve.Standard)
    val left by animateFloatAsState(edges.getOrElse(selected) { 0f }, slide, label = "Segment pill start")
    val right by animateFloatAsState(edges.getOrElse(selected + 1) { 0f }, slide, label = "Segment pill end")
    Row(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(track)
            .padding(ControlTokens.toggleInset)
            .drawBehind {
                if (selected < 0) return@drawBehind
                drawRoundRect(
                    OtusColors.selectedFill,
                    topLeft = Offset(left * size.width, 0f),
                    size = Size((right - left) * size.width, size.height),
                    cornerRadius = CornerRadius(size.height / 2),
                )
            }.selectableGroup(),
    ) {
        options.forEachIndexed { index, option ->
            val chosen = index == selected
            val color by animateColorAsState(
                if (chosen) OtusColors.text1 else OtusColors.text2,
                motionTween(MotionSpeed.Normal, MotionCurve.Standard),
                label = "Segment label",
            )
            Box(
                Modifier
                    .weight(shares[index])
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .selectable(chosen, role = Role.Tab) { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    option,
                    style = panelTextStyle(),
                    color = color,
                    fontSize = fontSize,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}
