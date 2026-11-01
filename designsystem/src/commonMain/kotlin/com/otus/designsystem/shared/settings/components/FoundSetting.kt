package com.otus.designsystem.shared.settings.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawOutline
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.motion.LocalMotionScale
import com.otus.designsystem.theme.motion.Motion
import com.otus.designsystem.theme.motion.MotionCurve
import com.otus.designsystem.theme.motion.MotionSpeed
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import kotlinx.coroutines.delay

/**
 * The title of the setting a search opened this page for, or null. Its [SettingRow] scrolls into
 * view and flashes once, so the user sees where the result is.
 */
public val LocalFoundSetting: ProvidableCompositionLocal<String?> = compositionLocalOf { null }

/** Scrolls the row into view and flashes it when [title] is the [LocalFoundSetting]. */
@Composable
internal fun Modifier.foundFlash(title: String): Modifier {
    if (LocalFoundSetting.current != title) return this
    val requester = remember { BringIntoViewRequester() }
    val flash = remember { Animatable(1f) }
    val scale = LocalMotionScale.current
    LaunchedEffect(Unit) {
        requester.bringIntoView()
        // The page slides in first, so the flash holds long enough to be seen once it settles.
        delay(Motion.millis(MotionSpeed.Relaxed, scale) * FLASH_HOLD_SPANS.toLong())
        flash.animateTo(
            0f,
            tween(Motion.millis(MotionSpeed.Relaxed, scale) * FLASH_FADE_SPANS, easing = MotionCurve.Standard.easing),
        )
    }
    val color = OtusColors.selectedFill
    val shape = RoundedCornerShape(SheetTokens.rowRadius)
    return bringIntoViewRequester(requester).drawBehind {
        drawOutline(shape.createOutline(size, layoutDirection, this), color, alpha = flash.value)
    }
}

private const val FLASH_HOLD_SPANS = 2
private const val FLASH_FADE_SPANS = 3
