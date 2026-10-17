package com.otus.designsystem.theme

import androidx.compose.ui.graphics.TransformOrigin

/** Size of the sidebar owl's view box in `brand/otus-sidebar-owl-*.svg`, in art units. */
internal const val OWL_ART_WIDTH = 120f
internal const val OWL_ART_HEIGHT = 132f

/** Fixed points of the sidebar owl, as fractions of its view box. */
internal object OwlArt {
    /** The owl tilts and hops around this point low on its body. */
    val tiltOrigin = artOrigin(60f, 69f)
    val leftEye = artOrigin(41.54f, 71.47f)
    val rightEye = artOrigin(80.22f, 63.25f)

    private fun artOrigin(
        x: Float,
        y: Float,
    ) = TransformOrigin(x / OWL_ART_WIDTH, y / OWL_ART_HEIGHT)
}

/**
 * One run of the sidebar owl, [millis] long at scale 1. Keyframes are fractions of [millis];
 * offsets are in art units and turns in degrees. [eyes] is the eyes' height, 1 when open; they fade
 * as they close, while the [happy] eyes show.
 */
internal class OwlTimeline(
    val millis: Int,
    val tilt: KeyframeTrack,
    val lift: KeyframeTrack,
    val glanceX: KeyframeTrack,
    val glanceY: KeyframeTrack,
    val eyes: KeyframeTrack,
    val happy: KeyframeTrack,
) {
    companion object {
        /** The eyes' height when the owl squints, where they have faded out completely. */
        const val EYES_CLOSED: Float = 0.1f

        /** `brand/otus-sidebar-owl-idle.svg`: the owl glances aside, and later tilts its head. */
        val Idle =
            OwlTimeline(
                millis = Motion.OWL_IDLE_MILLIS,
                tilt = KeyframeTrack(MotionCurve.Standard, 0f at 0.64f, 6f at 0.67f, 6f at 0.72f, 0f at 0.75f),
                lift = still(0f),
                glanceX = glance(-8.9f),
                glanceY = glance(0.9f),
                eyes = still(1f),
                happy = still(0f),
            )

        /** `brand/otus-sidebar-owl-smile.svg`: the owl hops and squints happily. */
        val Smile =
            OwlTimeline(
                millis = Motion.OWL_SMILE_MILLIS,
                tilt = KeyframeTrack(MotionCurve.Hop, 0f at 0f, -9f at 0.22f, 5f at 0.45f, 0f at 0.65f),
                lift = KeyframeTrack(MotionCurve.Hop, 0f at 0f, -7f at 0.22f, 0f at 0.45f),
                glanceX = still(0f),
                glanceY = still(0f),
                eyes =
                    KeyframeTrack(
                        MotionCurve.Standard,
                        1f at 0f,
                        EYES_CLOSED at 0.1f,
                        EYES_CLOSED at 0.82f,
                        1f at 1f,
                    ),
                happy = KeyframeTrack(MotionCurve.Standard, 0f at 0.06f, 1f at 0.14f, 1f at 0.8f, 0f at 0.92f),
            )

        private fun still(value: Float) = KeyframeTrack(MotionCurve.Standard, value at 0f, value at 1f)

        /** The glints in both eyes move by [offset] for a moment, as the owl looks aside. */
        private fun glance(offset: Float) =
            KeyframeTrack(MotionCurve.Standard, 0f at 0.22f, offset at 0.24f, offset at 0.29f, 0f at 0.31f)
    }
}
