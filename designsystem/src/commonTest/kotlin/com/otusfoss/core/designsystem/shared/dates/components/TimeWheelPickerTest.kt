package com.otusfoss.core.designsystem.shared.dates.components

import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test
    fun aRangeWithoutAFiveMinuteStepOffersItsStart() {
        assertTrue(stepTimes(LocalTime(10, 1)..LocalTime(10, 4)).isEmpty())
        assertEquals(listOf(LocalTime(10, 1)), pickableTimes(LocalTime(10, 1)..LocalTime(10, 4)))
        assertEquals(listOf(LocalTime(23, 58)), pickableTimes(LocalTime(23, 58)..LocalTime(23, 59)))
    }

    @Test
    fun aRangeWithStepsOffersOnlyThem() {
        val workday = LocalTime(9, 0)..LocalTime(17, 0)
        assertEquals(stepTimes(workday), pickableTimes(workday))
    }
}
