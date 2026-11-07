package com.otus.designsystem.screenshot

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import org.robolectric.shadows.ShadowDialog
import java.io.ByteArrayOutputStream

/** This view drawn into a new bitmap of its size. */
internal fun View.drawnBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    draw(Canvas(bitmap))
    return bitmap
}

/** The activity's window as drawn now, without menus or sheets, which have windows of their own. */
internal fun AndroidComposeTestRule<*, out ComponentActivity>.drawnWindow(): Bitmap =
    activity.window.decorView.drawnBitmap()

/** The activity's window once the UI is idle, as [drawnWindow] draws it. */
internal fun AndroidComposeTestRule<*, out ComponentActivity>.windowBitmap(): Bitmap = runOnIdle { drawnWindow() }

/** The window of the dialog shown last, such as a sheet or a picker, once the UI is idle. */
internal fun AndroidComposeTestRule<*, *>.dialogBitmap(): Bitmap =
    runOnIdle { requireNotNull(ShadowDialog.getLatestDialog().window).decorView.drawnBitmap() }

/** This bitmap encoded as a PNG. */
internal fun Bitmap.pngBytes(): ByteArray {
    val bytes = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, bytes)
    return bytes.toByteArray()
}

private const val PNG_QUALITY = 100
