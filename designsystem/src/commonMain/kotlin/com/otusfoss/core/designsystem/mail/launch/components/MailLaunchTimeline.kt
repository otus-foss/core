package com.otusfoss.core.designsystem.mail.launch.components

import com.otusfoss.core.designsystem.theme.motion.KeyframeTrack
import com.otusfoss.core.designsystem.theme.motion.Motion
import com.otusfoss.core.designsystem.theme.motion.MotionCurve
import com.otusfoss.core.designsystem.theme.motion.at

/**
 * One run of Mail's launch art, [millis] long at scale 1, with a track for each moving part.
 * Keyframes are fractions of [millis] and offsets are in art units. The tilt and blink use
 * [MotionCurve.Standard] where the SVGs ease in and out or run linearly.
 */
internal class MailLaunchTimeline(
    val millis: Int,
    val envelope: KeyframeTrack,
    val owl: KeyframeTrack,
    val tilt: KeyframeTrack,
    val blink: KeyframeTrack,
) {
    companion object {
        /** `brand/otus-mail-launch-once.svg`: everything arrives and settles, then blinks twice. */
        val Intro =
            MailLaunchTimeline(
                millis = Motion.LAUNCH_MILLIS,
                envelope = KeyframeTrack(MotionCurve.Rise, 70f at 0f, -2f at 0.13f, 0f at 0.18f),
                owl = KeyframeTrack(MotionCurve.Rise, 110f at 0.1f, -3f at 0.3f, 0f at 0.36f),
                tilt = KeyframeTrack(MotionCurve.Standard, 12f at 0.36f, -3f at 0.5f, 0f at 0.57f),
                blink = blinks(0.7f, 0.82f),
            )

        /** `brand/otus-mail-launch-loop.svg`: everything arrives, blinks twice, and drops away again. */
        val Loop =
            MailLaunchTimeline(
                millis = Motion.LAUNCH_LOOP_MILLIS,
                envelope =
                    KeyframeTrack(
                        MotionCurve.Rise,
                        70f at 0f,
                        -2f at 0.1f,
                        0f at 0.14f,
                        0f at 0.86f,
                        70f at 0.98f,
                    ),
                owl =
                    KeyframeTrack(
                        MotionCurve.Rise,
                        110f at 0.08f,
                        -3f at 0.22f,
                        0f at 0.27f,
                        0f at 0.82f,
                        110f at 0.94f,
                    ),
                tilt =
                    KeyframeTrack(
                        MotionCurve.Standard,
                        12f at 0.26f,
                        -3f at 0.36f,
                        0f at 0.41f,
                        0f at 0.8f,
                        12f at 0.9f,
                    ),
                blink = blinks(0.5f, 0.62f),
            )

        /** Where [Loop] has settled, in the same pose [Intro] ends in, so one can follow the other. */
        const val LOOP_SETTLED: Float = 0.41f

        /** How far a blink closes the eyes, as a fraction of their height. */
        private const val BLINK_SHUT = 0.08f

        /** How long a blink takes to close, and again to open, as a fraction of the run. */
        private const val BLINK_HALF = 0.02f

        /** Two blinks, each closing to [BLINK_SHUT] height and open again after twice [BLINK_HALF]. */
        private fun blinks(
            first: Float,
            second: Float,
        ) = KeyframeTrack(
            MotionCurve.Standard,
            1f at first,
            BLINK_SHUT at first + BLINK_HALF,
            1f at first + 2 * BLINK_HALF,
            1f at second,
            BLINK_SHUT at second + BLINK_HALF,
            1f at second + 2 * BLINK_HALF,
        )
    }
}
