package com.otus.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.lerp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.brand_mail_envelope
import com.otus.designsystem.resources.brand_mail_eye_left
import com.otus.designsystem.resources.brand_mail_eye_right
import com.otus.designsystem.resources.brand_mail_owl
import com.otus.designsystem.theme.BrandTokens
import com.otus.designsystem.theme.LocalMotionScale
import com.otus.designsystem.theme.MailLaunchArt
import com.otus.designsystem.theme.Motion
import com.otus.designsystem.theme.MotionCurve
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.offset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Mail's launch animation on the brand background: the envelope pops up, the owl rises behind it
 * and tilts upright. While [loading], it keeps looping. Once the owl has arrived and loading has
 * finished, or on a tap, the mark shrinks and fades out, and then [onFinished] runs. It takes
 * every touch meanwhile, so nothing beneath reacts.
 */
@Composable
public fun MailLaunch(
    loading: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motionScale = LocalMotionScale.current
    val playback = remember { MailLaunchPlayback() }
    val exit = remember { Animatable(0f) }
    var skipped by remember { mutableStateOf(false) }
    val stillLoading by rememberUpdatedState(loading)
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        val playing = launch { playback.play(motionScale) }
        snapshotFlow { skipped || (playback.settled && !stillLoading) }.first { it }
        playing.cancel()
        exit.animateTo(1f, Motion.tween(MotionSpeed.Relaxed, MotionCurve.Exit, motionScale))
        finish()
    }
    Box(
        modifier
            .fillMaxSize()
            .graphicsLayer { alpha = 1f - exit.value }
            .background(BrandTokens.background)
            .pointerInput(Unit) { detectTapGestures { skipped = true } },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(BrandTokens.launchMarkSize).graphicsLayer {
                val scale = lerp(1f, BrandTokens.LAUNCH_EXIT_SCALE, exit.value)
                scaleX = scale
                scaleY = scale
            },
        ) {
            Owl(playback)
            Layer(
                Res.drawable.brand_mail_envelope,
                Modifier.graphicsLayer {
                    translationY = playback.timeline.envelope.offset(playback.fraction, size.height)
                },
            )
        }
    }
}

@Composable
private fun Owl(playback: MailLaunchPlayback) {
    // The owl rises from below the envelope's lower edge, never through it.
    Box(Modifier.fillMaxSize().clipBelow(MailLaunchArt.OWL_CLIP)) {
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                translationY = playback.timeline.owl.offset(playback.fraction, size.height)
                rotationZ = playback.timeline.tilt.valueAt(playback.fraction)
                transformOrigin = MailLaunchArt.tiltOrigin
            },
        ) {
            Layer(Res.drawable.brand_mail_owl, Modifier)
            Eye(Res.drawable.brand_mail_eye_left, MailLaunchArt.leftEye, playback)
            Eye(Res.drawable.brand_mail_eye_right, MailLaunchArt.rightEye, playback)
        }
    }
}

@Composable
private fun Eye(
    eye: DrawableResource,
    center: TransformOrigin,
    playback: MailLaunchPlayback,
) = Layer(
    eye,
    Modifier.graphicsLayer {
        scaleY = playback.timeline.blink.valueAt(playback.fraction)
        transformOrigin = center
    },
)

@Composable
private fun Layer(
    image: DrawableResource,
    modifier: Modifier,
) = Image(painterResource(image), contentDescription = null, modifier = modifier.fillMaxSize())

/** Hides whatever this draws below [fraction] of its height. */
private fun Modifier.clipBelow(fraction: Float): Modifier =
    drawWithContent { clipRect(bottom = size.height * fraction) { this@drawWithContent.drawContent() } }
