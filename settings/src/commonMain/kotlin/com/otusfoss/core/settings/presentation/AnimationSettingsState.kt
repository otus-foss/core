package com.otusfoss.core.settings.presentation

import com.otusfoss.core.designsystem.theme.motion.AnimationSpeed
import com.otusfoss.core.storage.StoredChoice

/**
 * The user's animation speed. Provide its [speed]'s scale as `LocalMotionScale` so every animation
 * follows it; the appearance page calls [select]. An unknown saved name reads as Regular.
 */
public class AnimationSettingsState(
    store: StoredChoice,
) : StoredChoiceState<AnimationSpeed>(
        store,
        AnimationSpeed.Regular,
        { entryNamed<AnimationSpeed>(it.trim()) },
        { it.name.lowercase() },
    ) {
    public val speed: AnimationSpeed get() = value
}
