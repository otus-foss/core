package com.otus.designsystem.components

import kotlin.test.Test
import kotlin.test.assertEquals

class ValuePickerLabelTest {
    @Test
    fun keywordComesBeforeTheValue() {
        assertEquals("Move to Receipts, change", valuePickerLabel("Receipts", keyword = "Move to"))
    }

    @Test
    fun valueAloneWithoutKeyword() {
        assertEquals("Receipts, change", valuePickerLabel("Receipts", keyword = null))
    }
}
