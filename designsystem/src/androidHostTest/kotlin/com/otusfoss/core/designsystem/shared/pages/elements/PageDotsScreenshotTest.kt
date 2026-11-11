package com.otusfoss.core.designsystem.shared.pages.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otusfoss.core.designsystem.mail.folders.elements.DestinationPill
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_archive
import com.otusfoss.core.designsystem.resources.symbol_auto_filter
import com.otusfoss.core.designsystem.resources.symbol_inbox
import com.otusfoss.core.designsystem.screenshot.screenshot
import com.otusfoss.core.designsystem.shared.controls.elements.IconPill
import com.otusfoss.core.designsystem.theme.tokens.rows.StripTokens
import com.otusfoss.core.designsystem.theme.tokens.selection.MoveTokens
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PageDotsScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun positions() =
        compose.screenshot("page_dots") {
            Column(verticalArrangement = Arrangement.spacedBy(StripTokens.gap)) {
                PageDots(count = 6, current = 0)
                PageDots(count = 6, current = 4)
                PageDots(count = 20, current = 10)
            }
        }

    @Test
    fun destinationPills() =
        compose.screenshot("destination_pills") {
            Row {
                DestinationPill("Inbox", Res.drawable.symbol_inbox, {})
                DestinationPill("Archive", Res.drawable.symbol_archive, {})
                IconPill(
                    "Sort future mail",
                    Res.drawable.symbol_auto_filter,
                    selected = false,
                    onClick = {},
                    size = MoveTokens.pillHeight,
                )
            }
        }
}
