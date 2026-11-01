package com.otus.designsystem.shared.people.elements

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DecodableSizeTest {
    @Test
    fun photosOfAnySideDecode() {
        val photos = listOf(5712 to 4284, 8000 to 6000, 16_320 to 12_240, 30_000 to 4000, 4096 to 4096, 1 to 1)
        photos.forEach { (width, height) -> assertTrue(isDecodableSize(width, height), "$width x $height") }
    }

    @Test
    fun emptyAndImplausiblyLargeImagesDoNot() {
        val rejected = listOf(0 to 100, 100 to -1, 20_000 to 20_000, 70_000 to 70_000, Int.MAX_VALUE to Int.MAX_VALUE)
        rejected.forEach { (width, height) -> assertFalse(isDecodableSize(width, height), "$width x $height") }
    }
}
