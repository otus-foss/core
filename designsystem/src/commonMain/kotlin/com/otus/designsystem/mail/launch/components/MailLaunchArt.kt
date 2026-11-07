package com.otus.designsystem.mail.launch.components

import androidx.compose.ui.graphics.TransformOrigin
import com.otus.designsystem.theme.motion.KeyframeTrack

/** Side of the square the launch art is drawn in, matching the SVGs in `brand/`. */
private const val ART_SIZE = 128f

/** Fixed points of Mail's launch art, as fractions of its square. */
internal object MailLaunchArt {
    /** The owl tilts around this point near its feet. */
    val tiltOrigin = artOrigin(TILT_X, TILT_Y)
    val leftEye = artOrigin(LEFT_EYE_X, LEFT_EYE_Y)
    val rightEye = artOrigin(RIGHT_EYE_X, RIGHT_EYE_Y)

    /** The owl shows only above the envelope's lower part. */
    const val OWL_CLIP: Float = 108f / ART_SIZE

    private const val TILT_X = 64f
    private const val TILT_Y = 92f
    private const val LEFT_EYE_X = 48.84f
    private const val LEFT_EYE_Y = 59.43f
    private const val RIGHT_EYE_X = 80.6f
    private const val RIGHT_EYE_Y = 52.68f

    private fun artOrigin(
        x: Float,
        y: Float,
    ) = TransformOrigin(x / ART_SIZE, y / ART_SIZE)
}

/** The track's offset at [fraction], scaled from art units to a layer [height] in pixels. */
internal fun KeyframeTrack.offset(
    fraction: Float,
    height: Float,
): Float = valueAt(fraction) * height / ART_SIZE
