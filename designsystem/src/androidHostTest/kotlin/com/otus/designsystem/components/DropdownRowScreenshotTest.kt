package com.otus.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.theme.ComposeTokens
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DropdownRowScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val people =
        listOf(
            DemoPeople.mira to "mira@example.com",
            DemoPeople.tomas to "tomas.berg@example.com",
            DemoPeople.lena to "lena.park@studio-with-a-long-name.example",
        )

    // A long address stops at the detail's width and ends in an ellipsis, so the name stays readable.
    @Test
    fun peopleWithAddresses() =
        compose.screenshot("dropdown_row_people") {
            Column(Modifier.width(344.dp)) {
                people.forEach { (person, address) ->
                    DropdownRow(person.name, selected = false, onClick = {}, detail = address) {
                        PersonAvatar(person.avatar, ComposeTokens.dropdownAvatarSize)
                    }
                }
            }
        }
}
