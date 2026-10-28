package com.otus.designsystem.theme.brand

import androidx.compose.ui.graphics.TransformOrigin
import com.otus.designsystem.theme.motion.KeyframeTrack

/** Side of the square the launch art is drawn in, matching the SVGs in `brand/`. */
private const val ART_SIZE = 128f

/** Fixed points of Mail's launch art, as fractions of its square. */
internal object MailLaunchArt {
    /** The owl tilts around this point near its feet. */
    val tiltOrigin = artOrigin(64f, 92f)
    val leftEye = artOrigin(48.84f, 59.43f)
    val rightEye = artOrigin(80.6f, 52.68f)

    /** The owl shows only above the envelope's lower part. */
    const val OWL_CLIP: Float = 108f / ART_SIZE

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
