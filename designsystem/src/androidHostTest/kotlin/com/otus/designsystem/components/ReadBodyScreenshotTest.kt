package com.otus.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.elements.DatePill
import com.otus.designsystem.elements.FolderPath
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_archive
import com.otus.designsystem.resources.symbol_flag
import com.otus.designsystem.resources.symbol_forward
import com.otus.designsystem.resources.symbol_hide_image
import com.otus.designsystem.resources.symbol_image
import com.otus.designsystem.resources.symbol_notifications_off
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.theme.FolderColor
import com.otus.designsystem.theme.panelTextStyle
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReadBodyScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun banner() =
        compose.screenshot("image_banner") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ImageBanner(Res.drawable.symbol_hide_image, "Images blocked for privacy", "Show", {})
                ImageBanner(Res.drawable.symbol_image, "Images shown this time", "Always for Sentry", {})
            }
        }

    @Test
    fun details() =
        compose.screenshot("details_table") {
            DetailsTable(
                listOf(
                    DetailLine("From") { Text("Grace Hopper", style = panelTextStyle()) },
                    DetailLine("Date") { DatePill("12 Oct", "12 Oct 2026, 09:14", {}) },
                    DetailLine("Folders") { FolderPath(listOf("Finance", "Invoices")) },
                ),
            )
        }

    @Test
    fun quote() =
        compose.screenshot("quote_fold") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuoteFold(QuotedLevel(DemoPeople.mira, "10 Oct", QUOTE, onPerson = {}), expanded = false, onToggle = {})
                QuoteFold(QuotedLevel(null, "10 Oct", QUOTE), expanded = true, onToggle = {})
            }
        }

    @Test
    fun folderPicks() =
        compose.screenshot("folder_pick_row") {
            Column {
                FolderPickRow(DemoRead.clients, checked = true, {}, detail = "you're here")
                FolderPickRow(DemoRead.invoices, checked = true, {})
                FolderPickRow(DemoRead.archive, checked = false, {})
                FolderPickRow(
                    FolderName(
                        listOf("Freelancing", "Clients", "Long running invoices"),
                        FolderColor.Pink,
                        system = false,
                    ),
                    checked = false,
                    {},
                )
            }
        }

    @Test
    fun choicePicks() =
        compose.screenshot("pick_row_radio_first") {
            Column {
                PickRow(
                    "From people you know",
                    "Contacts, people you've written to and senders you allowed. Everyone else asks first.",
                    PickMark(selected = true, atStart = true),
                    {},
                    subtitleLines = 3,
                )
                PickRow("Never", "Always ask, for every mail.", PickMark(selected = false, atStart = true), {})
            }
        }

    @Test
    fun personChips() =
        compose.screenshot("person_chip") {
            val mira = DemoPeople.mira
            PersonChip(Recipient(mira.name, mira.avatar, mira.name))
        }

    @Test
    fun options() =
        compose.screenshot("options_list_read") {
            OptionsList(
                listOf(
                    OptionsItem.Action("Forward", Res.drawable.symbol_forward, {}),
                    OptionsItem.Action("Flag", Res.drawable.symbol_flag, {}),
                    OptionsItem.Divider,
                    OptionsItem.Caption("Whole conversation"),
                    OptionsItem.Action(
                        "Move to…",
                        Res.drawable.symbol_archive,
                        {},
                        opens = true,
                        detail = "Usually Invoices",
                    ),
                    OptionsItem.Toggle("Mute conversation", Res.drawable.symbol_notifications_off, true) {},
                ),
                onDone = {},
            )
        }

    private companion object {
        const val QUOTE =
            "I went through the plan with legal this morning. Short version: two waves work, but the EU wave " +
                "needs more lead time than we assumed. German terms need a full review, and the consent banner " +
                "has to ship before the EU wave."
    }
}
