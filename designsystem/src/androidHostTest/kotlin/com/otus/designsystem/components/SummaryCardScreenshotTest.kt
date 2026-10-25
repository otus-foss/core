package com.otus.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.elements.Avatar
import com.otus.designsystem.screenshot.screenshot
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SummaryCardScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun withLeading() =
        compose.screenshot("summary_card_leading") {
            SummaryCard("Lunch on Friday?", "Mira Chen · 9:41") { Avatar("MC") }
        }

    @Test
    fun people() =
        compose.screenshot("summary_card_people") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard("2 messages", listOf(DemoPeople.mira, DemoPeople.tomas))
                with(DemoPeople) { SummaryCard("5 messages", listOf(mira, tomas, lena, sam, noor)) }
            }
        }
}
