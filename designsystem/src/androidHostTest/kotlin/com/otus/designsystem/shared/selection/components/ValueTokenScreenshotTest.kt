package com.otus.designsystem.shared.selection.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_alternate_email
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.shared.people.components.DemoPeople
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ValueTokenScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun variants() =
        compose.screenshot("value_token_variants") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueToken("invoice")
                ValueToken("Tomas Berg", keyword = "From", avatar = DemoPeople.tomas.avatar)
                ValueToken("billing@example.com", keyword = "To", icon = Res.drawable.symbol_alternate_email)
                ValueToken("A very long subject line that cannot fit", Modifier.width(200.dp), keyword = "Subject")
            }
        }
}
