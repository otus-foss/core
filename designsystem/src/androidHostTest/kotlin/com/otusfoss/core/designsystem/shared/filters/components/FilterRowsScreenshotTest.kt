package com.otusfoss.core.designsystem.shared.filters.components

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_calendar_today
import com.otusfoss.core.designsystem.resources.symbol_folder
import com.otusfoss.core.designsystem.screenshot.screenshot
import com.otusfoss.core.designsystem.shared.people.components.DemoPeople
import com.otusfoss.core.designsystem.shared.people.components.PersonAvatar
import com.otusfoss.core.designsystem.shared.people.components.Recipient
import com.otusfoss.core.designsystem.shared.selection.components.PickMark
import com.otusfoss.core.designsystem.shared.selection.components.PickRow
import com.otusfoss.core.designsystem.shared.selection.components.PickStyle
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FilterRowsScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun valueRows() =
        compose.screenshot("filter_value_row") {
            FilterRowCard(
                listOf(
                    { FilterValueRow(Res.drawable.symbol_folder, "Folders", "Any", {}) },
                    {
                        FilterValueRow(
                            Res.drawable.symbol_calendar_today,
                            "Date",
                            "Last 7 days",
                            {},
                            set = true,
                        )
                    },
                ),
            )
        }

    @Test
    fun peopleEmpty() =
        compose.screenshot("people_line_empty") {
            FilterRowCard(
                listOf(
                    { PeopleLine("From", emptyList(), "Add people…", {}, {}) },
                    { PeopleLine("To", emptyList(), "Anyone", {}, {}) },
                ),
            )
        }

    @Test
    fun peopleWithChips() =
        compose.screenshot("people_line_chips") {
            val people =
                listOf(
                    Recipient("Grace Hopper", DemoPeople.grace, "Grace Hopper"),
                    Recipient("Ada Lovelace", DemoPeople.ada, "Ada Lovelace"),
                    Recipient("Linus Torvalds", DemoPeople.linus, "Linus Torvalds"),
                )
            FilterRowCard(
                listOf(
                    { PeopleLine("From", people, "Add people…", {}, {}) },
                    { PeopleLine("To", emptyList(), "Anyone", {}, {}) },
                ),
            )
        }

    @Test
    fun pickRows() =
        compose.screenshot("pick_row") {
            Column {
                PickRow(
                    "Grace Hopper",
                    "grace@navy.example · 12 conversations",
                    PickMark(selected = true, multiple = true, style = PickStyle.CheckCircle),
                    {},
                ) { PersonAvatar(DemoPeople.grace, 36.dp) }
                PickRow(
                    "Ada Lovelace",
                    "ada@analytical.example · 6 conversations",
                    PickMark(selected = false, multiple = true, style = PickStyle.CheckCircle),
                    {},
                ) { PersonAvatar(DemoPeople.ada, 36.dp) }
                PickRow(
                    "All accounts",
                    "2 accounts · 3 addresses",
                    PickMark(selected = false, style = PickStyle.CheckCircle),
                    {},
                )
            }
        }
}
