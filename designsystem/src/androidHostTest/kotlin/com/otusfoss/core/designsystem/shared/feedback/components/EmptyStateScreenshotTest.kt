package com.otusfoss.core.designsystem.shared.feedback.components

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_inbox
import com.otusfoss.core.designsystem.resources.symbol_search
import com.otusfoss.core.designsystem.shared.pages.components.pageScreenshot
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EmptyStateScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun emptyState() =
        compose.pageScreenshot("empty_state") {
            EmptyState("No results", "Try another word, or search all folders.", Res.drawable.symbol_search)
        }

    @Test
    fun emptyStateWithAction() =
        compose.pageScreenshot("empty_state_action") {
            EmptyState("Inbox zero", "New mail shows up here.", Res.drawable.symbol_inbox, actionLabel = "Refresh")
        }
}
