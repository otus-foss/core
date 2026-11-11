package com.otusfoss.core.designsystem.theme.colors

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class AccentPresetTest {
    @Test
    fun aPresetIsItsOwnClosestPreset() {
        AccentPreset.entries.forEach { assertEquals(it, AccentPreset.closest(it.color)) }
    }

    @Test
    fun aCustomColorPicksTheNearestPreset() {
        assertEquals(AccentPreset.Orange, AccentPreset.closest(Color(0xFFC75E38)))
        assertEquals(AccentPreset.Green, AccentPreset.closest(Color(0xFF2E9E5B)))
    }
}
