package com.otus.settings.presentation

import com.otus.designsystem.theme.AnimationSpeed
import com.otus.settings.domain.AnimationPreferenceStore
import kotlin.test.Test
import kotlin.test.assertEquals

class AnimationSettingsStateTest {
    @Test
    fun aSpeedIsAppliedAndRestoredForTheNextSession() {
        val store = TestAnimationStore()
        AnimationSettingsState(store).select(AnimationSpeed.Instant)
        assertEquals(AnimationSpeed.Instant, AnimationSettingsState(store).speed)
    }

    @Test
    fun withoutASavedSpeedAnimationsRunAtRegularSpeed() {
        assertEquals(AnimationSpeed.Regular, AnimationSettingsState(TestAnimationStore()).speed)
    }

    @Test
    fun anUnknownSpeedReadsAsRegular() {
        assertEquals(AnimationSpeed.Regular, decodeAnimationSpeed("ludicrous"))
        assertEquals(AnimationSpeed.Fast, decodeAnimationSpeed("fast"))
    }

    private class TestAnimationStore : AnimationPreferenceStore {
        private var value: String? = null

        override fun read(): String? = value

        override fun write(value: String) {
            this.value = value
        }
    }
}
