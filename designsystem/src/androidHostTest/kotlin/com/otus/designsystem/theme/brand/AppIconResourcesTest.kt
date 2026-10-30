package com.otus.designsystem.theme.brand

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.otus.designsystem.theme.colors.AccentPreset
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertNotEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppIconResourcesTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun everyPresetAndMonochromeHasALauncherIcon() {
        (AccentPreset.entries.map { it.iconName } + "monochrome").forEach { name ->
            val id = context.resources.getIdentifier("otus_mail_icon_$name", "mipmap", context.packageName)
            assertNotEquals(0, id, "tools/brand_drawables.py has no icon for $name")
        }
    }

    // OtusBlue becomes otus_blue, as tools/brand_drawables.py names the icons.
    private val AccentPreset.iconName: String
        get() = name.replace(Regex("(?<=.)([A-Z])"), "_$1").lowercase()
}
