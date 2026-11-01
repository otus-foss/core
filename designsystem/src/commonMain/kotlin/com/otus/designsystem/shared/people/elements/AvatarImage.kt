package com.otus.designsystem.shared.people.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Platform raster decoding only, shrunk so neither side exceeds [maxSize] pixels. The caller
 * supplies bytes; no network access occurs in the UI. A large photo decodes as well, sampled down
 * while it decodes, so memory stays bounded by [maxSize]; null for an image whose size
 * [isDecodableSize] rejects.
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

/**
 * Whether [decodeImage] takes an image of [width] by [height] pixels. Decoding samples it down, so
 * the sides are not limited, only the total: an image that claims more pixels than any camera takes
 * would keep the decoder busy for nothing. A 200-megapixel photo or a long panorama still decodes.
 */
internal fun isDecodableSize(
    width: Int,
    height: Int,
): Boolean = width > 0 && height > 0 && width.toLong() * height <= MAX_IMAGE_PIXELS

/** About 268 million pixels, above the 200 megapixels of the largest phone cameras. */
private const val MAX_IMAGE_PIXELS = 1L shl 28

private const val AVATAR_DECODE_SIZE = 256
private const val PREVIEW_DECODE_SIZE = 2048
