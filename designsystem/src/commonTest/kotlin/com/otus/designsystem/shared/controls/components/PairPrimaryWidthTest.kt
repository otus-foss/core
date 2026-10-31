package com.otus.designsystem.shared.controls.components

import kotlin.test.Test
import kotlin.test.assertEquals

class PairPrimaryWidthTest {
    @Test
    fun shortLabelsKeepTheUsualShare() = assertEquals(620, pairPrimaryWidth(1000, 200, 300))

    @Test
    fun aLongPrimaryLabelWidensUntilItFits() = assertEquals(700, pairPrimaryWidth(1000, 200, 700))

    @Test
    fun aLongSecondaryLabelNarrowsThePrimary() = assertEquals(550, pairPrimaryWidth(1000, 450, 300))

    @Test
    fun whenBothCannotFitTheSecondaryKeepsOneLine() = assertEquals(800, pairPrimaryWidth(1000, 200, 900))

    @Test
    fun aVeryLongSecondaryStopsAtItsUsualShare() = assertEquals(620, pairPrimaryWidth(1000, 600, 900))
}
