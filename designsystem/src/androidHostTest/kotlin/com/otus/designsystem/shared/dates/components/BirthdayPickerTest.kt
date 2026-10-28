package com.otus.designsystem.shared.dates.components

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BirthdayPickerTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aYearOlderThanTheWheelsUsualRangeIsKept() {
        assertEquals(YearlyDate(3, 15, 1900), confirmedUntouched(YearlyDate(3, 15, 1900)))
    }

    @Test
    fun aFutureYearIsKept() {
        assertEquals(YearlyDate(3, 15, 2999), confirmedUntouched(YearlyDate(3, 15, 2999)))
    }

    private fun confirmedUntouched(initial: YearlyDate): YearlyDate? {
        var confirmed: YearlyDate? = null
        compose.setContent { BirthdayPicker(initial, onDismiss = {}, onConfirm = { confirmed = it }) }
        compose.waitForIdle()
        compose.onNodeWithText("Done").performClick()
        return confirmed
    }
}
