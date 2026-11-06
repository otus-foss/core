package com.otus.settings.presentation

import com.otus.designsystem.theme.motion.AnimationSpeed
import com.otus.storage.inMemoryChoice
import kotlin.test.Test
import kotlin.test.assertEquals

class AnimationSettingsStateTest {
    @Test
    fun aSpeedIsAppliedAndRestoredForTheNextSession() {
        val store = inMemoryChoice()
        AnimationSettingsState(store).select(AnimationSpeed.Instant)
        assertEquals(AnimationSpeed.Instant, AnimationSettingsState(store).speed)
    }

    @Test
    fun withoutASavedSpeedAnimationsRunAtRegularSpeed() {
        assertEquals(AnimationSpeed.Regular, AnimationSettingsState(inMemoryChoice()).speed)
    }

    @Test
    fun anUnknownSpeedReadsAsRegular() {
        assertEquals(AnimationSpeed.Regular, AnimationSettingsState(inMemoryChoice("ludicrous")).speed)
        assertEquals(AnimationSpeed.Fast, AnimationSettingsState(inMemoryChoice("fast")).speed)
    }
}
