package com.otus.designsystem.components

import kotlin.test.Test
import kotlin.test.assertEquals

class ChoiceOptionsTest {
    @Test
    fun theDefaultComesFirstInAGroupOfItsOwn() {
        val choices = ChoiceOptions(listOf("Day", "Device", "Year"), selected = "Year", default = "Device")
        assertEquals(listOf(listOf("Device"), listOf("Day", "Year")), choices.groups)
    }

    @Test
    fun withoutAUsableDefaultEveryOptionSharesOneGroup() {
        assertEquals(listOf(listOf("Day", "Year")), ChoiceOptions(listOf("Day", "Year"), "Day").groups)
        assertEquals(
            listOf(listOf("Day", "Year")),
            ChoiceOptions(listOf("Day", "Year"), "Day", default = "Week").groups,
        )
    }

    @Test
    fun aDefaultThatIsTheOnlyOptionLeavesNoEmptyGroup() {
        assertEquals(listOf(listOf("Device")), ChoiceOptions(listOf("Device"), "Device", default = "Device").groups)
    }
}
