package com.otus.designsystem.components

import androidx.compose.ui.test.junit4.v2.createComposeRule
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
class AvatarStackScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun onePersonWithName() =
        compose.screenshot("avatar_stack_one") { AvatarStack(listOf(DemoPeople.ada), label = "Ada") }

    @Test
    fun overLimitWithCount() = compose.screenshot("avatar_stack_many") { AvatarStack(DemoPeople.all, label = "+1") }

    @Test
    fun chip() =
        compose.screenshot("avatar_stack_chip") {
            AvatarStackChip(DemoPeople.all.take(2), "Ada", "Ada and Grace", {})
        }

    @Test
    fun chipWithDetail() =
        compose.screenshot("avatar_stack_chip_detail") {
            AvatarStackChip(DemoPeople.all, "+1", "Ada and 4 others", {}, detail = "12")
        }
}
