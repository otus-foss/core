package com.otusfoss.core.designsystem.mail.read.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_archive
import com.otusfoss.core.designsystem.resources.symbol_newspaper
import com.otusfoss.core.designsystem.resources.symbol_unsubscribe
import com.otusfoss.core.designsystem.screenshot.screenshot
import com.otusfoss.core.designsystem.shared.controls.elements.UiAction
import com.otusfoss.core.designsystem.shared.feedback.components.InfoStrip
import com.otusfoss.core.designsystem.shared.folders.elements.FolderStack
import com.otusfoss.core.designsystem.shared.pages.elements.TurnProgress
import com.otusfoss.core.designsystem.shared.people.components.DemoPeople
import com.otusfoss.core.designsystem.shared.people.components.NamedAvatar
import com.otusfoss.core.designsystem.shared.rows.components.StripCard
import com.otusfoss.core.designsystem.shared.rows.elements.FirstFit
import com.otusfoss.core.designsystem.theme.colors.FolderColor
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.colors.ThemeBase
import com.otusfoss.core.designsystem.theme.colors.ThemeChoice
import com.otusfoss.core.designsystem.theme.tokens.content.ReadTokens
import com.otusfoss.core.designsystem.theme.tokens.rows.StripTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle
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

    /**
     * A swipe toward the second page: its dot swells while the strip keeps the first page, then
     * the strip shows the second once letting go would turn the page, and the dots alone in a pill.
     */
    @Test
    fun turning() =
        compose.screenshot("position_strip_turning") {
            val pages =
                listOf(
                    PositionPage(NamedAvatar("Latest", DemoPeople.grace), "1/6 · 12 Oct"),
                    PositionPage(NamedAvatar("Margaret Knuth", DemoPeople.margaret), "2/6 · 11 Oct"),
                )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0.6f, 1f).map { TurnProgress(toward = 1, fraction = it) }.forEach { turn ->
                    TurningPositionStrip(6, current = 0, turn, { pages[it.coerceAtMost(1)] }, {}) { FolderSlot() }
                    TurnDotsPill(count = 6, current = 0, turn)
                }
            }
        }

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
                    UiAction("Unsubscribe", Res.drawable.symbol_unsubscribe) {},
                )
                InfoStrip(
                    Res.drawable.symbol_newspaper,
                    "Unsubscribed",
                    "Takes up to 2 days · 23 older digests in your mail",
                    UiAction("Archive all", Res.drawable.symbol_archive) {},
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
        ) { FolderSlot() }
        PositionStrip(
            NamedAvatar("Margaret Hamilton-Knuth", DemoPeople.margaret),
            count = 24,
            current = 11,
            detail = "12/24 · Yesterday",
            onClick = {},
        ) { FolderSlot() }
    }
}

/** "also" with stacked folders and their count, shortening to fit beside the position. */
@Composable
private fun FolderSlot() {
    val stack = @Composable { FolderStack(listOf(FolderColor.Yellow, FolderColor.Pink, FolderColor.Green)) }
    val count = @Composable { Text("4 folders", style = panelTextStyle(), fontSize = ReadTokens.previewSize) }
    val also =
        @Composable {
            Text(
                "also",
                style = panelTextStyle(),
                color = OtusColors.text3,
                fontSize = ReadTokens.previewSize,
            )
        }
    FirstFit(
        listOf(
            {
                SlotRow {
                    also()
                    stack()
                    count()
                }
            },
            {
                SlotRow {
                    stack()
                    count()
                }
            },
            stack,
        ),
    )
}

@Composable
private fun SlotRow(content: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(StripTokens.glyphGap),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}
