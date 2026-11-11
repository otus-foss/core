package com.otusfoss.core.designsystem.shared.people.elements

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.otusfoss.core.designsystem.screenshot.screenshot
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AvatarScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun initialsByColor() =
        compose.screenshot("avatar_initials_colors") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { Avatar("AL", fill = avatarFill(it)) }
            }
        }

    @Test
    fun sizes() =
        compose.screenshot("avatar_sizes") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(20.dp, 32.dp, 64.dp).forEach { Avatar("AL", size = it) }
            }
        }

    @Test
    fun ring() =
        compose.screenshot("avatar_ring") {
            Avatar("GH", Modifier.border(BorderStroke(2.dp, OtusColors.accentLight), CircleShape))
        }

    @Test
    fun decodedImage() {
        val image = assertNotNull(decodeAvatarImage(gradientPng()))
        compose.screenshot("avatar_image") { Avatar("AL", image = image) }
    }

    @Test
    fun decodedImageIsDownsampled() {
        val image = assertNotNull(decodeAvatarImage(gradientPng(width = 1024)))
        assertEquals(256, image.width)
    }

    @Test
    fun anImageWiderThan4096PixelsIsDownsampledToo() {
        val image = assertNotNull(decodeImage(gradientPng(width = 6000, height = 120), maxSize = 160))
        assertTrue(image.width in 1..160, "Width ${image.width}")
    }

    @Test
    fun invalidBytesDecodeToNull() = assertNull(decodeAvatarImage(byteArrayOf(1, 2, 3)))

    private fun gradientPng(
        width: Int = 96,
        height: Int = width,
    ): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val paint =
            Paint().apply {
                shader =
                    LinearGradient(
                        0f,
                        0f,
                        width.toFloat(),
                        height.toFloat(),
                        Color.rgb(242, 153, 74),
                        Color.rgb(86, 204, 242),
                        Shader.TileMode.CLAMP,
                    )
            }
        Canvas(bitmap).drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return ByteArrayOutputStream()
            .apply { bitmap.compress(Bitmap.CompressFormat.PNG, 100, this) }
            .toByteArray()
    }
}
