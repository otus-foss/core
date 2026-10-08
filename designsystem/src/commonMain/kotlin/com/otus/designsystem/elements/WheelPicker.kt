package com.otus.designsystem.elements

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.WheelTokens
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
    LaunchedEffect(selected, options.size) {
        positioning = true
        try {
            scroll.animateScrollToItem(selected)
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
        val rowHeight = maxOf(PanelTokens.touchTarget, with(density) { WheelTokens.lineHeight.toDp() })
        val padding = ((maxHeight - rowHeight) / 2).coerceAtLeast(WheelTokens.zeroInset)
        LazyColumn(
            Modifier
                .fillMaxSize()
                .selectableGroup()
                .semantics { contentDescription = "$title choices" }
                .wheelGuides(rowHeight.value * density.density),
            state = scroll,
            contentPadding = PaddingValues(vertical = padding),
            flingBehavior = rememberSnapFlingBehavior(scroll),
        ) {
            items(options.size, key = { options[it] }) { index ->
                WheelOption(options[index], index, selected, { choose(index) }, Modifier.height(rowHeight))
            }
        }
    }
}

private fun LazyListState.centeredIndex(): Int? {
    val layout = layoutInfo
    val center = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
    return layout.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - center) }?.index
}

@Composable
private fun WheelOption(
    label: String,
    index: Int,
    selected: Int,
    onSelect: () -> Unit,
    modifier: Modifier,
) {
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .fillMaxWidth()
            .selectable(index == selected, interactions, indication = null, role = Role.RadioButton, onClick = onSelect)
            .controlPressFeedback(interactions),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            label,
            style =
                LocalTextStyle.current.copy(
                    color =
                        when (abs(index - selected)) {
                            0 -> OtusColors.text1
                            1 -> OtusColors.text2
                            else -> OtusColors.text3
                        },
                    fontSize = WheelTokens.textSize,
                    lineHeight = WheelTokens.lineHeight,
                    textAlign = TextAlign.Center,
                ),
            maxLines = 1,
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
                strokeWidth = PanelTokens.borderWidth.toPx(),
            )
        }
    }
