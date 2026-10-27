package com.otus.designsystem.shared.selection.components

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_archive
import com.otus.designsystem.resources.symbol_delete
import com.otus.designsystem.resources.symbol_flag
import com.otus.designsystem.screenshot.screenScreenshot
import com.otus.designsystem.shared.people.components.DemoPeople
import com.otus.designsystem.shared.people.components.NamedAvatar
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChoiceSheetScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val formats = listOf("Device format", "31.12.2024", "12/31/2024", "2024-12-31")

    @Test
    fun withDefaultAndFootnote() =
        compose.screenScreenshot("choice_sheet_default") {
            ChoiceSheet(
                "Date format",
                ChoiceOptions(formats, selected = "2024-12-31", default = "Device format"),
                row = { ChoiceRow(it, detail = "Uses the phone's region".takeIf { _ -> it == "Device format" }) },
                onSelect = {},
                onDismiss = {},
                footnote = "Message lists and headers use this format.",
            )
        }

    @Test
    fun withIcons() =
        compose.screenScreenshot("choice_sheet_icons") {
            val icons = mapOf("Archive" to Res.drawable.symbol_archive, "Delete" to Res.drawable.symbol_delete)
            ChoiceSheet(
                "Swipe left",
                ChoiceOptions(listOf("Archive", "Delete", "Flag"), selected = "Delete"),
                row = { ChoiceRow(it, icon = icons[it] ?: Res.drawable.symbol_flag) },
                onSelect = {},
                onDismiss = {},
            )
        }

    @Test
    fun withPeopleAndKeywords() =
        compose.screenScreenshot("choice_sheet_people") {
            val people = mapOf("ada@example.com" to "Ada Lovelace", "grace@example.com" to "Grace Hopper")
            ChoiceSheet(
                "Send from",
                ChoiceOptions(people.keys.toList(), selected = "ada@example.com"),
                row = { address ->
                    val name = people.getValue(address)
                    val avatar = if (address.startsWith("ada")) DemoPeople.ada else DemoPeople.grace
                    ChoiceRow(name, detail = address, person = NamedAvatar(name, avatar), keyword = "From")
                },
                onSelect = {},
                onDismiss = {},
            )
        }

    @Test
    fun choosingReportsTheOption() {
        val chosen = mutableListOf<String>()
        compose.setContent {
            ChoiceSheet("Theme", ChoiceOptions(listOf("Ink", "Slate"), "Ink"), { ChoiceRow(it) }, { chosen += it }, {})
        }
        compose.onNodeWithText("Slate").performClick()
        assertEquals(listOf("Slate"), chosen)
    }
}
