package com.otus.designsystem.shared.selection.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import com.otus.designsystem.screenshot.screenScreenshot
import com.otus.designsystem.theme.colors.ThemeBase
import com.otus.designsystem.theme.colors.ThemeChoice
import com.otus.designsystem.theme.tokens.overlays.MenuTokens
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DropdownScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    // The rows scroll to the last address while the search stays at the top.
    @Test
    fun headerStaysWhileScrolledLight() = scrolledWithHeader("dropdown_header_scrolled_light", ThemeBase.Light)

    @Test
    fun headerStaysWhileScrolledBlack() = scrolledWithHeader("dropdown_header_scrolled_black", ThemeBase.Black)

    private fun scrolledWithHeader(
        name: String,
        theme: ThemeBase,
    ) = compose.screenScreenshot(
        name,
        act = { onNodeWithText(addresses.last()).performScrollTo() },
        palette = ThemeChoice(theme).palette,
    ) {
        // OtusTheme's Surface stretches its child, so the anchor sits in a wrapper at the top.
        Box {
            Box(Modifier.padding(16.dp)) {
                Dropdown(
                    {},
                    unrolls = false,
                    header = {
                        PickerSearch(
                            "",
                            {},
                            "Search ${addresses.size} addresses",
                            Modifier.padding(MenuTokens.inset),
                        )
                    },
                ) {
                    ListCaption("Work")
                    addresses.forEach {
                        PickRow(it, null, PickMark(false, style = PickStyle.Card), {})
                    }
                }
            }
        }
    }

    private val addresses = (1..20).map { "person$it@example.com" }
}
