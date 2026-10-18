package com.otus.designsystem.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.TextUnit
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SignatureTokens
import com.otus.designsystem.theme.motionTween
import com.otus.designsystem.theme.panelTextStyle

/**
 * A row of [options] in one track, one of them [selected] with a `bgLight` fill, such as Copy from
 * and Copy to; none while [selected] is -1. Each label stays on one line at [fontSize].
 */
@Composable
public fun SegmentedToggle(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(SignatureTokens.toggleHeight)
            .clip(CircleShape)
            .background(OtusColors.bgRegular)
            .padding(SignatureTokens.toggleInset)
            .selectableGroup(),
    ) {
        options.forEachIndexed { index, option ->
            val chosen = index == selected
            val fill by animateColorAsState(
                if (chosen) OtusColors.bgLight else OtusColors.bgRegular,
                motionTween(MotionSpeed.Fast),
                label = "Segment fill",
            )
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(fill)
                    .selectable(chosen, role = Role.Tab) { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    option,
                    style = panelTextStyle(),
                    color = if (chosen) OtusColors.text1 else OtusColors.text2,
                    fontSize = fontSize,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}
