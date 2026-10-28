package com.otus.settings.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.otus.designsystem.theme.motion.AnimationSpeed
import com.otus.settings.domain.AnimationPreferenceStore

/**
 * The user's animation speed. Provide its [speed]'s scale as `LocalMotionScale` so every animation
 * follows it; the appearance page calls [select].
 */
public class AnimationSettingsState(
    private val store: AnimationPreferenceStore? = null,
    initialSpeed: AnimationSpeed = store?.read()?.let(::decodeAnimationSpeed) ?: AnimationSpeed.Regular,
) {
    public var speed: AnimationSpeed by mutableStateOf(initialSpeed)
        private set

    public fun select(speed: AnimationSpeed) {
        store?.write(speed.name.lowercase())
        this.speed = speed
    }
}

/** An unknown name reads as [AnimationSpeed.Regular]. */
internal fun decodeAnimationSpeed(value: String): AnimationSpeed =
    AnimationSpeed.entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) } ?: AnimationSpeed.Regular
