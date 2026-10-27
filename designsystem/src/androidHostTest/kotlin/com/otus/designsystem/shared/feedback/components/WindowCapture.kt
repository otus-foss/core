package com.otus.designsystem.shared.feedback.components

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import java.io.File

/**
 * Saves the window to `build/reports/toasts/[name].png`, where a motion's steps can be compared by
 * eye. A paused clock never finishes the redraw `captureToImage` waits for, so this draws the
 * window itself.
 */
internal fun AndroidComposeTestRule<*, out ComponentActivity>.captureWindow(name: String) {
    val out = File("build/reports/toasts").apply { mkdirs() }
    runOnUiThread {
        val view = activity.window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        File(out, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, LOSSLESS, it) }
    }
}

/** PNG ignores the quality value; 100 states that nothing is lost. */
private const val LOSSLESS = 100
