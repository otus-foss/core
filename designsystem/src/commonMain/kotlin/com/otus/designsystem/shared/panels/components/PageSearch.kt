package com.otus.designsystem.shared.panels.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.otus.designsystem.theme.motion.LocalMotionScale
import com.otus.designsystem.theme.motion.Motion
import com.otus.designsystem.theme.motion.MotionSpeed

/** A page's search: the [query], how to change it, and the field's [placeholder], such as "Search settings". */
public class PageSearch(
    public val query: String,
    public val onQueryChange: (String) -> Unit,
    public val placeholder: String,
)

/**
 * The expansion's time toward [open], from 0 to 1 over [MotionSpeed.Relaxed] at the user's
 * animation speed. It runs linearly: the field's growth eases on the enter curve itself, so closing
 * retraces the opening frame for frame.
 */
@Composable
public fun rememberSearchExpansion(open: Boolean): () -> Float {
    val progress = remember { Animatable(if (open) 1f else 0f) }
    val scale = LocalMotionScale.current
    LaunchedEffect(open, scale) {
        progress.animateTo(
            if (open) 1f else 0f,
            tween(Motion.millis(MotionSpeed.Relaxed, scale), easing = LinearEasing),
        )
    }
    return { progress.value }
}
