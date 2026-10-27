package com.otus.designsystem.shared.settings.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.ThemeTokens

/**
 * A slider over a colored [track], such as a hue rainbow. Dragging or tapping the track sets a
 * [value] within [range]; [label] names it for screen readers, which can also set it directly.
 */
@Composable
public fun ColorSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    track: Brush,
    label: String,
    modifier: Modifier = Modifier,
    range: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    val change by rememberUpdatedState(onValueChange)
    val span = range.endInclusive - range.start
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(ThemeTokens.sliderHeight)
            .semantics {
                contentDescription = label
                progressBarRangeInfo = ProgressBarRangeInfo(value, range)
                setProgress { target ->
                    change(target.coerceIn(range))
                    true
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        val thumb = with(LocalDensity.current) { ThemeTokens.sliderThumb.toPx() }
        val width = constraints.maxWidth.toFloat()
        // The thumb's center travels between half a thumb from either end.
        val valueAt = { x: Float -> range.start + ((x - thumb / 2) / (width - thumb)).coerceIn(0f, 1f) * span }
        Box(
            Modifier
                .matchParentSize()
                .background(track, CircleShape)
                .pointerInput(range, width) { detectTapGestures { change(valueAt(it.x)) } }
                .pointerInput(range, width) {
                    detectHorizontalDragGestures {
                        pointer,
                        _,
                        ->
                        change(valueAt(pointer.position.x))
                    }
                },
        )
        val fraction = ((value - range.start) / span).coerceIn(0f, 1f)
        Box(
            Modifier
                .offset(x = (maxWidth - ThemeTokens.sliderThumb) * fraction)
                .size(ThemeTokens.sliderThumb)
                .background(OtusColors.text1, CircleShape)
                .border(ThemeTokens.sliderThumbBorder, OtusColors.bgDark, CircleShape),
        )
    }
}
