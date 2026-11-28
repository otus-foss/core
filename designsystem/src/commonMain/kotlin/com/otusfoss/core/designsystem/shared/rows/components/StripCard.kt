package com.otusfoss.core.designsystem.shared.rows.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.motion.MotionCurve
import com.otusfoss.core.designsystem.theme.motion.MotionSpeed
import com.otusfoss.core.designsystem.theme.motion.motionTween
import com.otusfoss.core.designsystem.theme.shapes.SmoothCornerShape
import com.otusfoss.core.designsystem.theme.shapes.strokeBorder
import com.otusfoss.core.designsystem.theme.tokens.core.Sizes
import com.otusfoss.core.designsystem.theme.tokens.rows.StripTokens

/**
 * The card of a strip above the action bar, such as the thread or attachment strip: full width,
 * rounded like the bar, `bgDark` with a `strokeBgDark` border. Its size animates as its content
 * changes, so a strip at the bottom of a column grows upward in place. The caller adds the side
 * margin and the gaps between strips. A strip whose height follows a finger uses
 * [UnfoldingStripCard] instead, since a second animation would trail behind the finger.
 * [frame] fades the fill and border, from 1 (shown) to 0, where only the content remains.
 */
@Composable
internal fun StripCard(
    modifier: Modifier = Modifier,
    frame: Float = 1f,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!LocalStripFramed.current) return Column(modifier.fillMaxWidth(), content = content)
    Column(
        modifier
            .fillMaxWidth()
            .animateContentSize(motionTween(MotionSpeed.Normal, MotionCurve.Standard))
            .stripCardFrame(frame),
        content = content,
    )
}

/**
 * A strip card's full width, rounded `bgDark` fill, and `strokeBgDark` border, without the size
 * animation. [frame] is how much of the fill and border shows.
 */
internal fun Modifier.stripCardFrame(frame: Float = 1f): Modifier {
    val shape = SmoothCornerShape(StripTokens.radius)
    val fill = OtusColors.bgDark
    val border = OtusColors.strokeBgDark
    return fillMaxWidth()
        .clip(shape)
        .background(fill.copy(alpha = fill.alpha * frame))
        .strokeBorder(Sizes.borderWidth, border.copy(alpha = border.alpha * frame), shape)
}

/** False inside an [UnfoldingStripCard]'s folded form, whose card already frames the strip drawn there. */
internal val LocalStripFramed = staticCompositionLocalOf { true }
