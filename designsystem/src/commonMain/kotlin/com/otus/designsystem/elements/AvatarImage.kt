package com.otus.designsystem.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Platform raster decoding only, shrunk so neither side exceeds [maxSize] pixels. The caller
 * supplies bytes; no network access occurs in the UI.
 */
public expect fun decodeImage(
    bytes: ByteArray,
    maxSize: Int,
): ImageBitmap?

/** [decodeImage] at avatar size. */
public fun decodeAvatarImage(bytes: ByteArray): ImageBitmap? = decodeImage(bytes, AVATAR_DECODE_SIZE)

/** Decodes off the main thread. Rows that scroll back into view reuse the decoded bitmap. */
@Composable
public fun rememberAvatarImage(bytes: ByteArray?): ImageBitmap? = rememberAvatarImage(bytes, decodedAvatars)

@Composable
internal fun rememberAvatarImage(
    bytes: ByteArray?,
    cache: DecodedAvatarCache,
): ImageBitmap? {
    val image = remember(bytes, cache) { mutableStateOf(bytes?.let(cache::get)) }
    LaunchedEffect(bytes, cache) {
        image.value = bytes?.let { cache.load(it) }
    }
    return image.value
}

private val decodedAvatars = DecodedAvatarCache()

/** Decodes [bytes] off the main thread at most [maxSize] pixels on a side, such as a photo to preview. */
@Composable
public fun rememberImage(
    bytes: ByteArray?,
    maxSize: Int = PREVIEW_DECODE_SIZE,
): ImageBitmap? {
    val image = remember(bytes) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(bytes, maxSize) {
        image.value = bytes?.let { withContext(Dispatchers.Default) { decodeImage(it, maxSize) } }
    }
    return image.value
}

private const val AVATAR_DECODE_SIZE = 256
private const val PREVIEW_DECODE_SIZE = 2048
