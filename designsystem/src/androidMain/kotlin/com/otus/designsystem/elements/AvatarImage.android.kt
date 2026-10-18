package com.otus.designsystem.elements

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
        if (options.outWidth !in 1..MAX_IMAGE_DIMENSION || options.outHeight !in 1..MAX_IMAGE_DIMENSION) {
            return@runCatching null
        }
        options.inJustDecodeBounds = false
        options.inSampleSize = 1
        while (maxOf(options.outWidth, options.outHeight) / options.inSampleSize > maxSize) {
            options.inSampleSize *= SAMPLE_MULTIPLIER
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.asImageBitmap()
    }.getOrNull()

private const val MAX_IMAGE_DIMENSION = 4096
private const val SAMPLE_MULTIPLIER = 2
