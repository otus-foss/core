package com.otus.designsystem.shared.controls.elements

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
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
        transitionSpec = { roll(if (targetState.order >= initialState.order) 1 else -1, motionScale) using null },
        label = "Text replacement",
    ) { label ->
        Text(label.text, style = style, textAlign = textAlign)
    }
}

/**
 * A one-line label whose changed words roll, as [AnimatedText] does: a higher number slides up and
 * a lower one down, and the words after it ease to their new place. Unchanged words stay still.
 */
@Composable
public fun RollingWords(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    val motionScale = LocalMotionScale.current
    val words = text.split(' ')
    Row(modifier.clipToBounds()) {
        words.forEachIndexed { index, word ->
            AnimatedContent(
                targetState = if (index < words.lastIndex) "$word " else word,
                transitionSpec = {
                    roll(rollDirection(initialState, targetState), motionScale) using
                        SizeTransform(clip = false) { _, _ -> textEnterSpec(motionScale) }
                },
                label = "Rolling word",
            ) { Text(it, style = style, maxLines = 1, softWrap = false) }
        }
    }
}

/** Down for a lower number, up for anything else. */
private fun rollDirection(
    from: String,
    to: String,
): Int {
    val old = from.trim().toLongOrNull()
    val new = to.trim().toLongOrNull()
    return if (old != null && new != null && new < old) -1 else 1
}

/** The new text slides in from below, or above for a [direction] of -1, as the old slides out. */
private fun roll(
    direction: Int,
    motionScale: Float,
): ContentTransform =
    (slideInVertically(textEnterSpec(motionScale)) { direction * it } + fadeIn(textEnterSpec(motionScale)))
        .togetherWith(
            slideOutVertically(textExitSpec(motionScale)) { -direction * it } + fadeOut(textExitSpec(motionScale)),
        )

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
