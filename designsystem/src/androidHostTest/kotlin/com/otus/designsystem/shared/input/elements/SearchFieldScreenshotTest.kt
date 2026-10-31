package com.otus.designsystem.shared.input.elements

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
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
class SearchFieldScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun placeholder() = compose.screenshot("search_field_placeholder") { SearchField("", {}, "Search Inbox", WIDTH) }

    @Test
    fun query() =
        compose.screenshot("search_field_query") { SearchField("receipts from Ada", {}, "Search Inbox", WIDTH) }

    private companion object {
        val WIDTH = Modifier.width(240.dp)
    }
}
