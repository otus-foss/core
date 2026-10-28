package com.otus.designsystem.shared.controls.elements

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.otus.designsystem.theme.motion.LocalMotionScale
import com.otus.designsystem.theme.motion.Motion
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.MotionSpeed

/**
 * Replace a label with a clipped vertical slide and fade. Increasing order moves upward;
 * decreasing order moves downward. The caller supplies ordering, without calendar knowledge here.
 * Unchanged text does not animate. [LocalMotionScale] and the system animator scale set the pace.
 */
@Composable
public fun AnimatedText(
    text: String,
    order: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign = TextAlign.Center,
) {
    val motionScale = LocalMotionScale.current
    AnimatedContent(
        targetState = OrderedLabel(text, order),
        modifier = modifier.clipToBounds().clearAndSetSemantics { this.text = AnnotatedString(text) },
        contentKey = { it.text },
        contentAlignment = Alignment.Center,
        transitionSpec = {
            val direction = if (targetState.order >= initialState.order) 1 else -1
            val incoming =
                slideInVertically(textEnterSpec(motionScale)) { direction * it } + fadeIn(textEnterSpec(motionScale))
            val outgoing =
                slideOutVertically(textExitSpec(motionScale)) { -direction * it } + fadeOut(textExitSpec(motionScale))
            (incoming togetherWith outgoing).using(null)
        },
        label = "Text replacement",
    ) { label ->
        Text(label.text, style = style, textAlign = textAlign)
    }
}

/** New text arrives at the normal speed. */
internal fun <T> textEnterSpec(motionScale: Float): TweenSpec<T> =
    Motion.tween(MotionSpeed.Normal, MotionCurve.Enter, motionScale)

/** Old text leaves faster than new text arrives, so the two overlap only briefly. */
internal fun <T> textExitSpec(motionScale: Float): TweenSpec<T> =
    Motion.tween(MotionSpeed.Fast, MotionCurve.Exit, motionScale)

private data class OrderedLabel(
    val text: String,
    val order: Int,
)
