package com.otus.designsystem.mail.launch.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.lerp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.brand_mail_beak
import com.otus.designsystem.resources.brand_mail_envelope
import com.otus.designsystem.resources.brand_mail_envelope_lines
import com.otus.designsystem.resources.brand_mail_eye_left
import com.otus.designsystem.resources.brand_mail_eye_left_shine
import com.otus.designsystem.resources.brand_mail_eye_right
import com.otus.designsystem.resources.brand_mail_eye_right_shine
import com.otus.designsystem.resources.brand_mail_owl
import com.otus.designsystem.theme.AppIconStyle
import com.otus.designsystem.theme.BrandTokens
import com.otus.designsystem.theme.LocalMotionScale
import com.otus.designsystem.theme.MailLaunchArt
import com.otus.designsystem.theme.Motion
import com.otus.designsystem.theme.MotionCurve
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.offset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Mail's launch animation in the current theme: the owl and envelope in [style]'s color on
 * [OtusColors.bgDark]. The envelope pops up, the owl rises behind it and tilts upright. While
 * [loading], it keeps looping. As soon as loading has finished, even before the owl has arrived, or
 * on a tap, the mark shrinks and fades out, and then [onFinished] runs. It takes every touch
 * meanwhile, so nothing beneath reacts.
 */
@Composable
public fun MailLaunch(
    style: AppIconStyle,
    loading: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motionScale = LocalMotionScale.current
    val mark = style.markColor
    val playback = remember { MailLaunchPlayback() }
    val exit = remember { Animatable(0f) }
    var skipped by remember { mutableStateOf(false) }
    val stillLoading by rememberUpdatedState(loading)
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        val playing = launch { playback.play(motionScale) }
        snapshotFlow { skipped || !stillLoading }.first { it }
        playing.cancel()
        exit.animateTo(1f, Motion.tween(MotionSpeed.Relaxed, MotionCurve.Exit, motionScale))
        finish()
    }
    Box(
        modifier
            .fillMaxSize()
            .graphicsLayer { alpha = 1f - exit.value }
            .background(OtusColors.bgDark)
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
            Owl(playback, mark)
            Box(
                Modifier.fillMaxSize().graphicsLayer {
                    translationY = playback.timeline.envelope.offset(playback.fraction, size.height)
                },
            ) {
                Layer(Res.drawable.brand_mail_envelope, mark)
                Layer(Res.drawable.brand_mail_envelope_lines, OtusColors.bgDark)
            }
        }
    }
}

@Composable
private fun Owl(
    playback: MailLaunchPlayback,
    mark: Color,
) {
    // The owl rises from below the envelope's lower edge, never through it.
    Box(Modifier.fillMaxSize().clipBelow(MailLaunchArt.OWL_CLIP)) {
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                translationY = playback.timeline.owl.offset(playback.fraction, size.height)
                rotationZ = playback.timeline.tilt.valueAt(playback.fraction)
                transformOrigin = MailLaunchArt.tiltOrigin
            },
        ) {
            Layer(Res.drawable.brand_mail_owl, mark)
            Eye(LeftEye, playback, mark)
            Eye(RightEye, playback, mark)
            Layer(Res.drawable.brand_mail_beak, OtusColors.bgDark)
        }
    }
}

/** An eye's dark disc, its light shine, and the point it blinks around. */
private class EyeArt(
    val disc: DrawableResource,
    val shine: DrawableResource,
    val center: TransformOrigin,
)

private val LeftEye =
    EyeArt(Res.drawable.brand_mail_eye_left, Res.drawable.brand_mail_eye_left_shine, MailLaunchArt.leftEye)
private val RightEye =
    EyeArt(Res.drawable.brand_mail_eye_right, Res.drawable.brand_mail_eye_right_shine, MailLaunchArt.rightEye)

@Composable
private fun Eye(
    eye: EyeArt,
    playback: MailLaunchPlayback,
    mark: Color,
) = Box(
    Modifier.fillMaxSize().graphicsLayer {
        scaleY = playback.timeline.blink.valueAt(playback.fraction)
        transformOrigin = eye.center
    },
) {
    Layer(eye.disc, OtusColors.bgDark)
    Layer(eye.shine, mark)
}

/** One single-color layer of the launch art, painted in [color]. */
@Composable
private fun Layer(
    image: DrawableResource,
    color: Color,
) = Image(
    painterResource(image),
    contentDescription = null,
    modifier = Modifier.fillMaxSize(),
    colorFilter = ColorFilter.tint(color),
)

/** Hides whatever this draws below [fraction] of its height. */
private fun Modifier.clipBelow(fraction: Float): Modifier =
    drawWithContent { clipRect(bottom = size.height * fraction) { this@drawWithContent.drawContent() } }
