package com.otus.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.otus.designsystem.theme.MailLaunchTimeline
import com.otus.designsystem.theme.Motion

/** Where the launch animation is: the intro once, then the loop for as long as [play] runs. */
@Stable
internal class MailLaunchPlayback {
    var timeline by mutableStateOf(MailLaunchTimeline.Intro)
        private set
    private val progress = Animatable(0f)

    /** How far through [timeline] the animation is. */
    val fraction: Float get() = progress.value

    /** Whether the owl has arrived, so the launch can end. */
    val settled: Boolean
        get() = timeline !== MailLaunchTimeline.Intro || fraction >= MailLaunchTimeline.INTRO_SETTLED

    /** Plays the intro, then the loop from its settled pose, until cancelled. */
    suspend fun play(scale: Float) {
        run(1f, scale)
        timeline = MailLaunchTimeline.Loop
        progress.snapTo(MailLaunchTimeline.LOOP_SETTLED)
        while (true) {
            run(1f, scale)
            progress.snapTo(0f)
        }
    }

    private suspend fun run(
        to: Float,
        scale: Float,
    ) {
        val millis = Motion.scaled(timeline.millis * (to - fraction), scale)
        progress.animateTo(to, TweenSpec(millis, easing = LinearEasing))
    }
}
