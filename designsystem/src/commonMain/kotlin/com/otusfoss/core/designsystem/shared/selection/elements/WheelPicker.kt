package com.otusfoss.core.designsystem.shared.selection.elements

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.shared.controls.elements.controlPressFeedback
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.motion.MotionCurve
import com.otusfoss.core.designsystem.theme.motion.MotionSpeed
import com.otusfoss.core.designsystem.theme.motion.animateScrollToItem
import com.otusfoss.core.designsystem.theme.motion.motionTween
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.selection.WheelTokens
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlin.math.abs

/** A bounded wheel. Tapping or scrolling chooses the centered option. */
@Composable
internal fun WheelPicker(
    title: String,
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberLazyListState(selected)
    val choose by rememberUpdatedState(onSelect)
    var positioning by remember { mutableStateOf(false) }
    val turn = motionTween<Float>(MotionSpeed.Normal, MotionCurve.Standard)
    LaunchedEffect(selected, options.size) {
        positioning = true
        try {
            scroll.animateScrollToItem(selected, turn)
        } finally {
            positioning = false
        }
    }
    LaunchedEffect(scroll) {
        snapshotFlow { if (scroll.isScrollInProgress || positioning) null else scroll.centeredIndex() }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { choose(it) }
    }
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val rowHeight = maxOf(Sizes.touchTarget, with(density) { WheelTokens.lineHeight.toDp() })
        val padding = ((maxHeight - rowHeight) / 2).coerceAtLeast(0.dp)
        LazyColumn(
            Modifier
                .fillMaxSize()
                .selectableGroup()
                .semantics { contentDescription = "$title choices" }
                .wheelGuides(with(density) { rowHeight.toPx() }),
            state = scroll,
            contentPadding = PaddingValues(vertical = padding),
            flingBehavior = rememberSnapFlingBehavior(scroll),
        ) {
            items(options.size, key = { options[it] }) { index ->
                WheelOption(
                    options[index],
                    selected = index == selected,
                    distance = { scroll.rowsFromCenter(index) },
                    onSelect = { choose(index) },
                    modifier = Modifier.height(rowHeight),
                )
            }
        }
    }
}

private fun LazyListState.centeredIndex(): Int? {
    val layout = layoutInfo
    val center = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
    return layout.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - center) }?.index
}

/**
 * How many rows [index] sits from the wheel's center right now, also mid-scroll; far for a row
 * off screen.
 */
private fun LazyListState.rowsFromCenter(index: Int): Float {
    val layout = layoutInfo
    val item = layout.visibleItemsInfo.firstOrNull { it.index == index } ?: return FAR
    val center = (layout.viewportStartOffset + layout.viewportEndOffset) / 2f
    return abs(item.offset + item.size / 2f - center) / item.size
}

/**
 * One option, colored by its live [distance] from the center: `text1` there, `text2` a row away,
 * and `text3` from two rows, blending in between. The color is read while drawing, so scrolling
 * only redraws it.
 */
@Composable
private fun WheelOption(
    label: String,
    selected: Boolean,
    distance: () -> Float,
    onSelect: () -> Unit,
    modifier: Modifier,
) {
    val near = OtusColors.text1
    val middle = OtusColors.text2
    val far = OtusColors.text3
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .fillMaxWidth()
            .selectable(selected, interactions, indication = null, role = Role.RadioButton, onClick = onSelect)
            .controlPressFeedback(interactions),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            label,
            style =
                LocalTextStyle.current.copy(
                    fontSize = WheelTokens.textSize,
                    lineHeight = WheelTokens.lineHeight,
                    textAlign = TextAlign.Center,
                ),
            maxLines = 1,
            color = {
                val rows = distance()
                if (rows <= 1f) lerp(near, middle, rows) else lerp(middle, far, (rows - 1f).coerceAtMost(1f))
            },
            autoSize =
                TextAutoSize.StepBased(
                    minFontSize = WheelTokens.minTextSize,
                    maxFontSize = WheelTokens.textSize,
                ),
        )
    }
}

private fun Modifier.wheelGuides(rowHeight: Float): Modifier =
    drawWithContent {
        drawContent()
        val top = (size.height - rowHeight) / 2
        val bottom = (size.height + rowHeight) / 2
        val fade =
            Brush.verticalGradient(
                0f to OtusColors.bgRegular,
                WheelTokens.FADE_START to OtusColors.bgRegular.copy(alpha = 0f),
                WheelTokens.FADE_END to OtusColors.bgRegular.copy(alpha = 0f),
                1f to OtusColors.bgRegular,
            )
        drawRect(fade)
        listOf(top, bottom).forEach { y ->
            drawLine(
                OtusColors.bgLight,
                Offset(0f, y),
                Offset(size.width, y),
                strokeWidth = Sizes.borderWidth.toPx(),
            )
        }
    }

/** Beyond two rows, where every option is already `text3`. */
private const val FAR = 2f
