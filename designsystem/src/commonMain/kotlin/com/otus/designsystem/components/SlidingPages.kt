package com.otus.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.otus.designsystem.theme.LocalMotionScale
import com.otus.designsystem.theme.Motion
import com.otus.designsystem.theme.MotionCurve
import com.otus.designsystem.theme.MotionSpeed

/**
 * Shows [page] and slides the next one in from the side, pushing the current one out. A page
 * deeper than the current one, by [depth], comes in from the end edge; a shallower one returns
 * from the start edge on [MotionCurve.Reverse], so closing a page retraces its opening. Pages at
 * the same depth, such as sidebar destinations, come in from the end.
 */
@Composable
public fun <T> SlidingPages(
    page: T,
    depth: (T) -> Int,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    val motionScale = LocalMotionScale.current
    AnimatedContent(
        targetState = page,
        modifier = modifier,
        transitionSpec = {
            val back = depth(targetState) < depth(initialState)
            val direction = if (back) SlideDirection.End else SlideDirection.Start
            val spec = pageSlide<IntOffset>(back, motionScale)
            slideIntoContainer(direction, spec) togetherWith slideOutOfContainer(direction, spec)
        },
        label = "Page",
    ) { content(it) }
}

/** The tween of every page slide; going [back] retraces the opening on [MotionCurve.Reverse]. */
internal fun <T> pageSlide(
    back: Boolean,
    motionScale: Float,
): TweenSpec<T> = Motion.tween(MotionSpeed.Normal, if (back) MotionCurve.Reverse else MotionCurve.Standard, motionScale)
