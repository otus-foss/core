package com.otus.designsystem.shared.rows.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.motion.motionTween
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.shapes.strokeBorder
import com.otus.designsystem.theme.tokens.content.ReadTokens
import com.otus.designsystem.theme.tokens.layout.PanelTokens

/**
 * The card of a strip above the action bar, such as the thread or attachment strip: full width,
 * rounded like the bar, `bgDark` with a `strokeBgDark` border. Its size animates as its content
 * changes, so a strip at the bottom of a column grows upward in place. The caller adds the side
 * margin and the gaps between strips. A strip whose height follows a finger uses
 * [UnfoldingStripCard] instead, since a second animation would trail behind the finger.
 */
@Composable
public fun StripCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!LocalStripFramed.current) return Column(modifier.fillMaxWidth(), content = content)
    Column(
        modifier
            .fillMaxWidth()
            .animateContentSize(motionTween(MotionSpeed.Normal, MotionCurve.Standard))
            .stripCardFrame(),
        content = content,
    )
}

/** A strip card's full width, rounded `bgDark` fill, and `strokeBgDark` border, without the size animation. */
internal fun Modifier.stripCardFrame(): Modifier {
    val shape = SmoothCornerShape(ReadTokens.stripRadius)
    return fillMaxWidth()
        .clip(shape)
        .background(OtusColors.bgDark)
        .strokeBorder(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape)
}

/** False inside an [UnfoldingStripCard]'s folded form, whose card already frames the strip drawn there. */
internal val LocalStripFramed = staticCompositionLocalOf { true }
