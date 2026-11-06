package com.otus.designsystem.shared.contacts.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IndexLetterPreviewTest {
    @Test
    fun twoShortNamesShowTogether() {
        assertEquals("Jean, John", IndexLetter("J", count = 2, names = listOf("Jean", "John")).preview)
    }

    @Test
    fun aLongFirstNameShowsAlone() {
        assertEquals("Maximiliano", IndexLetter("M", count = 2, names = listOf("Maximiliano", "Max")).preview)
    }

    @Test
    fun aLetterWithoutNamesHasNoPreview() {
        assertNull(IndexLetter("K", count = 2).preview)
    }
}
