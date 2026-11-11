package com.otusfoss.core.designsystem.mail.folders.components

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.otusfoss.core.designsystem.shared.pages.components.pageScreenshot
import com.otusfoss.core.designsystem.theme.colors.FolderColor
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FolderColorNudgeScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun nudge() =
        compose.pageScreenshot("folder_color_nudge") {
            FolderColorNudge(
                title = "Receipts has no colour yet.",
                body = "Coloured folders are easier to spot next to each mail.",
                colors = FolderColor.entries - FolderColor.Default,
                onPick = {},
                onDismiss = {},
            )
        }
}
