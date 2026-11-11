package com.otusfoss.core.designsystem.shared.onboarding.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.otusfoss.core.designsystem.shared.controls.elements.RollingWords
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.motion.MotionSpeed
import com.otusfoss.core.designsystem.theme.motion.motionTween
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens
import com.otusfoss.core.designsystem.theme.tokens.onboarding.OnboardingTokens
import com.otusfoss.core.designsystem.theme.typography.TypeScale

/**
 * How far a run of [steps] has come: one segment per step, filled up to and including [step],
 * which counts from 1, then "[step] of [steps]". When [step] changes, the fill grows or shrinks
 * across the segments and the number rolls, as long as the same progress stays on screen. Screen
 * readers hear it as one sentence.
 */
@Composable
public fun StepProgress(
    step: Int,
    steps: Int,
    modifier: Modifier = Modifier,
) {
    val filled by animateFloatAsState(step.toFloat(), motionTween(MotionSpeed.Normal), label = "Step progress")
    val fill = OtusColors.text1
    val labelStyle = LocalTextStyle.current.copy(color = OtusColors.text3, fontSize = TypeScale.label)
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = PageTokens.textInset)
            .clearAndSetSemantics { contentDescription = "Step $step of $steps" },
        horizontalArrangement = Arrangement.spacedBy(OnboardingTokens.progressGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(OnboardingTokens.segmentGap)) {
            repeat(steps) { index ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(OnboardingTokens.segmentHeight)
                        .clip(CircleShape)
                        .background(OtusColors.bgLight)
                        // Read while drawing, so the animation redraws the segment without recomposing it.
                        .drawBehind {
                            val width = size.width * (filled - index).coerceIn(0f, 1f)
                            // The fill grows from the edge reading starts at.
                            val start = if (layoutDirection == LayoutDirection.Rtl) size.width - width else 0f
                            drawRect(fill, Offset(start, 0f), Size(width, size.height))
                        },
                )
            }
        }
        RollingWords("$step of $steps", Modifier.widthIn(min = widestLabel(steps, labelStyle)), labelStyle)
    }
}

/**
 * The width of the widest "n of [steps]" label in [style]. The digits differ in width, so the
 * label keeps this much room and the segments beside it never change size as the step changes.
 */
@Composable
private fun widestLabel(
    steps: Int,
    style: TextStyle,
): Dp {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(steps, style, density) {
        with(density) { (1..steps).maxOf { measurer.measure("$it of $steps", style).size.width }.toDp() }
    }
}
