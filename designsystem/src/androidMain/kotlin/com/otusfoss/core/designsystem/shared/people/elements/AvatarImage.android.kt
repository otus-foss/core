package com.otusfoss.core.designsystem.shared.people.elements

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

public actual fun decodeImage(
    bytes: ByteArray,
    maxSize: Int,
): ImageBitmap? =
    runCatching {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        if (!isDecodableSize(options.outWidth, options.outHeight)) return@runCatching null
        options.inJustDecodeBounds = false
        options.inSampleSize = 1
        // Sampling while decoding keeps memory to about maxSize squared, however large the image.
        while (maxOf(options.outWidth, options.outHeight) / options.inSampleSize > maxSize) {
            options.inSampleSize *= SAMPLE_MULTIPLIER
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.asImageBitmap()
    }.getOrNull()

private const val SAMPLE_MULTIPLIER = 2
