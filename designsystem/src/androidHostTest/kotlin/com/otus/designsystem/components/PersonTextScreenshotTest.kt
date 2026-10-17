package com.otus.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
class PersonTextScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun withAndWithoutMatch() =
        compose.screenshot("person_text_variants") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PersonText("Mail from Mira Chen usually goes to Travel", DemoPeople.mira)
                PersonText("Mail from Lena Park usually goes to Travel", DemoPeople.sam)
                PersonText("No sender named", null)
            }
        }
}
