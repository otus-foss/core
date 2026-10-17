package com.otus.designsystem.components

import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeWheelPickerTest {
    @Test
    fun onlyFiveMinuteStepsInsideTheRangeShow() {
        val times = stepTimes(LocalTime(14, 57)..LocalTime(15, 10))
        assertEquals(listOf(LocalTime(15, 0), LocalTime(15, 5), LocalTime(15, 10)), times)
    }

    @Test
    fun aWholeDayStartsAtMidnightAndEndsAtFiveToMidnight() {
        val times = stepTimes(LocalTime(0, 0)..LocalTime(23, 59))
        assertEquals(LocalTime(0, 0), times.first())
        assertEquals(LocalTime(23, 55), times.last())
        assertEquals(24 * 12, times.size)
    }
}
