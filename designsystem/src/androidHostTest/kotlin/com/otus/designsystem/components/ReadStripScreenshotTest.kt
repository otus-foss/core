package com.otus.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.elements.FolderStack
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_archive
import com.otus.designsystem.resources.symbol_newspaper
import com.otus.designsystem.resources.symbol_unsubscribe
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.theme.FolderColor
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.ReadTokens
import com.otus.designsystem.theme.ThemeBase
import com.otus.designsystem.theme.ThemeChoice
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
class ReadStripScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun position() = compose.screenshot("position_strip") { PositionStrips() }

    @Test
    fun positionLight() =
        compose.screenshot("position_strip_light", palette = ThemeChoice(ThemeBase.Light).palette) { PositionStrips() }

    @Test
    fun threadList() =
        compose.screenshot("thread_list") {
            StripCard {
                ThreadList("5 messages", DemoRead.thread, current = 3, onSelect = {}, onFold = {}, maxHeight = 400.dp)
            }
        }

    @Test
    fun alsoIn() =
        compose.screenshot("also_in_strip") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AlsoInStrip(listOf(DemoRead.invoices), {})
                AlsoInStrip(listOf(DemoRead.invoices.copy(path = listOf("Invoices")), DemoRead.freelancing), {})
                AlsoInStrip(
                    listOf(
                        DemoRead.invoices.copy(path = listOf("Invoices")),
                        DemoRead.freelancing,
                        DemoRead.clients,
                        DemoRead.archive,
                    ),
                    {},
                )
            }
        }

    @Test
    fun info() =
        compose.screenshot("info_strip") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoStrip(
                    Res.drawable.symbol_newspaper,
                    "Sentry weekly digest",
                    "Mailing list · about once a week",
                    StripAction("Unsubscribe", Res.drawable.symbol_unsubscribe) {},
                )
                InfoStrip(
                    Res.drawable.symbol_newspaper,
                    "Unsubscribed",
                    "Takes up to 2 days · 23 older digests in your mail",
                    StripAction("Archive all", Res.drawable.symbol_archive) {},
                )
            }
        }
}

@Composable
private fun PositionStrips() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PositionStrip(
            NamedAvatar("Latest", DemoPeople.grace),
            count = 6,
            current = 0,
            detail = "1/6 · 12 Oct",
            onClick = {},
        )
        PositionStrip(
            NamedAvatar("Margaret Knuth", DemoPeople.margaret),
            count = 6,
            current = 4,
            detail = "5/6 · 10 Oct",
            onClick = {},
        ) {
            Text("also", style = panelTextStyle(), color = OtusColors.text3, fontSize = ReadTokens.previewSize)
            FolderStack(listOf(FolderColor.Yellow, FolderColor.Pink, FolderColor.Green))
            Text("4 folders", style = panelTextStyle(), fontSize = ReadTokens.previewSize)
        }
    }
}
