package com.otus.settings.presentation

import com.otus.designsystem.theme.motion.AnimationSpeed
import kotlin.test.Test
import kotlin.test.assertEquals

class AnimationSettingsStateTest {
    @Test
    fun aSpeedIsAppliedAndRestoredForTheNextSession() {
        val store = MemoryPreferenceStore()
        AnimationSettingsState(store).select(AnimationSpeed.Instant)
        assertEquals(AnimationSpeed.Instant, AnimationSettingsState(store).speed)
    }

    @Test
    fun withoutASavedSpeedAnimationsRunAtRegularSpeed() {
        assertEquals(AnimationSpeed.Regular, AnimationSettingsState(MemoryPreferenceStore()).speed)
    }

    @Test
    fun anUnknownSpeedReadsAsRegular() {
        assertEquals(AnimationSpeed.Regular, decodeAnimationSpeed("ludicrous"))
        assertEquals(AnimationSpeed.Fast, decodeAnimationSpeed("fast"))
    }
}
