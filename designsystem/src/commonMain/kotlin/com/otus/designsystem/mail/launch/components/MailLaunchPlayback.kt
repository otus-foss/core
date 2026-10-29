package com.otus.designsystem.mail.launch.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.otus.designsystem.theme.brand.MailLaunchTimeline
import com.otus.designsystem.theme.motion.Motion

/** Where the launch animation is: the intro once, then the loop for as long as [play] runs. */
@Stable
internal class MailLaunchPlayback {
    var timeline by mutableStateOf(MailLaunchTimeline.Intro)
        private set
    private val progress = Animatable(0f)

    /** How far through [timeline] the animation is. */
    val fraction: Float get() = progress.value

    /** Plays the intro, then the loop from its settled pose, until cancelled. */
    suspend fun play(scale: Float) {
        playToEnd(scale)
        timeline = MailLaunchTimeline.Loop
        progress.snapTo(MailLaunchTimeline.LOOP_SETTLED)
        while (true) {
            playToEnd(scale)
            progress.snapTo(0f)
        }
    }

    /** Plays the rest of [timeline], from [fraction] to its end. */
    private suspend fun playToEnd(scale: Float) {
        val millis = Motion.scaled(timeline.millis * (1f - fraction), scale)
        progress.animateTo(1f, TweenSpec(millis, easing = LinearEasing))
    }
}
