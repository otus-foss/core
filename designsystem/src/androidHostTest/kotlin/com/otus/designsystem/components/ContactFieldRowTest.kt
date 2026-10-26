package com.otus.designsystem.components

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.otus.designsystem.theme.PanelTokens
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ContactFieldRowTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun theLabelStarAndRemoveButtonsHaveSeparateFullSizeTargets() {
        compose.setContent {
            ContactFieldRow(
                value = "ada@example.com",
                onValueChange = {},
                description = "Email 1",
                look = ContactFieldLook(label = "Work", labelChoices = emptyList()),
                modifier = Modifier.width(360.dp),
                primary = PrimaryStar(on = true, onClick = {}),
                onRemove = {},
            )
        }
        val targets =
            listOf("Email 1 label: Work. Change", "Primary", "Remove Email 1").map {
                compose.onNodeWithContentDescription(it).getBoundsInRoot()
            }

        targets.forEach { bounds ->
            assertTrue(bounds.width >= PanelTokens.touchTarget, "$bounds is narrower than a touch target")
            assertTrue(bounds.height >= PanelTokens.touchTarget, "$bounds is shorter than a touch target")
        }
        targets.zipWithNext().forEach { (first, second) ->
            assertTrue(!first.overlaps(second), "$first overlaps $second")
        }
    }
}

private fun DpRect.overlaps(other: DpRect): Boolean =
    left < other.right && other.left < right && top < other.bottom && other.top < bottom
