package com.otus.designsystem.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.otus.designsystem.theme.MotionCurve
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.ReadTokens
import com.otus.designsystem.theme.motionTween

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
    val shape = RoundedCornerShape(ReadTokens.stripRadius)
    return fillMaxWidth()
        .clip(shape)
        .background(OtusColors.bgDark)
        .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape)
}

/** False inside an [UnfoldingStripCard]'s folded form, whose card already frames the strip drawn there. */
internal val LocalStripFramed = staticCompositionLocalOf { true }
