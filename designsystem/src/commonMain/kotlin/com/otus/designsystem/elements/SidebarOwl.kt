package com.otus.designsystem.elements

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.InfiniteAnimationPolicy
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.brand_owl_beak
import com.otus.designsystem.resources.brand_owl_body
import com.otus.designsystem.resources.brand_owl_eye_left
import com.otus.designsystem.resources.brand_owl_eye_right
import com.otus.designsystem.resources.brand_owl_glint_left
import com.otus.designsystem.resources.brand_owl_glint_right
import com.otus.designsystem.resources.brand_owl_happy
import com.otus.designsystem.theme.KeyframeTrack
import com.otus.designsystem.theme.LocalMotionScale
import com.otus.designsystem.theme.Motion
import com.otus.designsystem.theme.OWL_ART_HEIGHT
import com.otus.designsystem.theme.OWL_ART_WIDTH
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.OwlArt
import com.otus.designsystem.theme.OwlTimeline
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * The Otus owl, which glances aside now and then. A tap makes it hop and smile. Its width follows
 * from its height.
 */
@Composable
public fun SidebarOwl(modifier: Modifier = Modifier) {
    val motionScale = LocalMotionScale.current
    val playback = remember { OwlPlayback() }
    var smiles by remember { mutableIntStateOf(0) }
    LaunchedEffect(motionScale, smiles) {
        // With animations off, the owl rests instead of looping without frames.
        if (motionScale.isInfinite()) return@LaunchedEffect
        if (smiles > 0) playback.run(OwlTimeline.Smile, motionScale)

        // Tests and other hosts that stop endless animations can stop the idle loop this way.
        suspend fun idle(): Nothing {
            while (true) playback.run(OwlTimeline.Idle, motionScale)
        }
        coroutineContext[InfiniteAnimationPolicy]?.onInfiniteOperation { idle() } ?: idle()
    }
    Box(
        modifier
            .aspectRatio(OWL_ART_WIDTH / OWL_ART_HEIGHT)
            .semantics { contentDescription = "Otus owl" }
            .clickable(remember { MutableInteractionSource() }, indication = null) { smiles++ }
            // The art faces the other way, as the SVGs mirror it.
            .graphicsLayer { scaleX = -1f },
    ) {
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                rotationZ = playback.value { tilt }
                translationY = playback.value { lift } * size.height / OWL_ART_HEIGHT
                transformOrigin = OwlArt.tiltOrigin
            },
        ) {
            Layer(Res.drawable.brand_owl_body, colorFilter = ColorFilter.tint(OtusColors.accentRegular))
            Layer(Res.drawable.brand_owl_beak)
            Eye(Res.drawable.brand_owl_eye_left, Res.drawable.brand_owl_glint_left, OwlArt.leftEye, playback)
            Eye(Res.drawable.brand_owl_eye_right, Res.drawable.brand_owl_glint_right, OwlArt.rightEye, playback)
            Layer(Res.drawable.brand_owl_happy, Modifier.graphicsLayer { alpha = playback.value { happy } })
        }
    }
}

/** Which run the owl is in, and how far through it. */
@Stable
private class OwlPlayback {
    private var timeline by mutableStateOf(OwlTimeline.Idle)
    private val progress = Animatable(0f)

    /** The current value of one of the timeline's tracks. */
    fun value(track: OwlTimeline.() -> KeyframeTrack): Float = timeline.track().valueAt(progress.value)

    suspend fun run(
        next: OwlTimeline,
        scale: Float,
    ) {
        timeline = next
        progress.snapTo(0f)
        progress.animateTo(1f, TweenSpec(Motion.scaled(next.millis.toFloat(), scale), easing = LinearEasing))
    }
}

/**
 * One eye, which squints around its [center]; its [glint], in the accent like the body, moves as
 * the owl glances aside.
 */
@Composable
private fun Eye(
    eye: DrawableResource,
    glint: DrawableResource,
    center: TransformOrigin,
    playback: OwlPlayback,
) = Box(
    Modifier.fillMaxSize().graphicsLayer {
        val open = playback.value { eyes }
        scaleY = open
        alpha = ((open - OwlTimeline.EYES_CLOSED) / (1f - OwlTimeline.EYES_CLOSED)).coerceIn(0f, 1f)
        transformOrigin = center
    },
) {
    Layer(eye)
    Layer(
        glint,
        colorFilter = ColorFilter.tint(OtusColors.accentRegular),
        modifier =
            Modifier.graphicsLayer {
                translationX = playback.value { glanceX } * size.width / OWL_ART_WIDTH
                translationY = playback.value { glanceY } * size.height / OWL_ART_HEIGHT
            },
    )
}

@Composable
private fun Layer(
    image: DrawableResource,
    modifier: Modifier = Modifier,
    colorFilter: ColorFilter? = null,
) = Image(
    painterResource(image),
    contentDescription = null,
    modifier = modifier.fillMaxSize(),
    colorFilter = colorFilter,
)
