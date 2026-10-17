package com.otus.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.otus.designsystem.theme.LocalMotionScale
import com.otus.designsystem.theme.Motion
import com.otus.designsystem.theme.MotionCurve
import com.otus.designsystem.theme.MotionSpeed

/**
 * Shows [page] and slides the next one in from the side, pushing the current one out. A page
 * deeper than the current one, by [depth], comes in from the end edge; a shallower one returns
 * from the start edge on [MotionCurve.Reverse], so closing a page retraces its opening. Pages at
 * the same depth, such as sidebar destinations, come in from the end. Chrome that both pages draw,
 * such as their [ContextPanel] and action bar, stays in place while the rest of the page slides.
 */
@Composable
public fun <T> SlidingPages(
    page: T,
    depth: (T) -> Int,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    val motionScale = LocalMotionScale.current
    val parent = LocalPageMotion.current
    val sign = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
    val host = rememberChromeHost(parent)
    val pages = updateTransition(page, label = "Page")
    val motions = remember { mutableStateMapOf<T, PageMotion>() }
    // Each page slides itself, so its chrome can undo the slide. The arriving page draws on top.
    pages.AnimatedContent(modifier.hostingChrome(parent, host), transitionSpec = { selfSliding }) { shown ->
        val back = depth(pages.targetState) < depth(pages.currentState)
        val slide =
            transition.animateFloat({ pageSlide(back, motionScale) }, label = "Page slide") {
                when (it) {
                    EnterExitState.PreEnter -> if (back) -1f else 1f
                    EnterExitState.Visible -> 0f
                    EnterExitState.PostExit -> if (back) 1f else -1f
                }
            }
        val motion =
            remember(parent, host, sign) {
                PageMotion(
                    parent,
                    host,
                    sign,
                    fraction = { slide.value },
                    leaving = { transition.targetState == EnterExitState.PostExit },
                    partner = {
                        val other = if (shown == pages.targetState) pages.currentState else pages.targetState
                        if (other == shown) null else motions[other]
                    },
                )
            }
        DisposableEffect(shown, motion) {
            motions[shown] = motion
            onDispose { motions.remove(shown) }
        }
        Box(Modifier.slidingPage(motion)) {
            CompositionLocalProvider(LocalPageMotion provides motion) { content(shown) }
        }
    }
}

/** Leaves the slide to the pages; the page slide's own animation keeps a leaving page until it ends. */
private val selfSliding = ContentTransform(EnterTransition.None, ExitTransition.None, sizeTransform = null)

/** The tween of every page slide; going [back] retraces the opening on [MotionCurve.Reverse]. */
internal fun <T> pageSlide(
    back: Boolean,
    motionScale: Float,
): TweenSpec<T> = Motion.tween(MotionSpeed.Normal, if (back) MotionCurve.Reverse else MotionCurve.Standard, motionScale)
