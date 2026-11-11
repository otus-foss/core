package com.otusfoss.core.avatar

import kotlinx.coroutines.CancellationException

/** Optional avatar failures never hide mail/events, and cancellation always propagates. */
internal suspend fun avatarOrNull(load: suspend () -> AvatarImage?): AvatarImage? =
    nullOnFailure(load)?.takeIf { it.bytes.isNotEmpty() && it.bytes.size <= MAX_AVATAR_BYTES && !it.isPixel() }

/** A 1×1 PNG, which icon services such as Bulwark answer for a domain without an icon. */
private fun AvatarImage.isPixel(): Boolean {
    if (bytes.size < PNG_IHDR_END || !PNG_SIGNATURE.indices.all { bytes[it] == PNG_SIGNATURE[it] }) return false
    val width = bigEndianInt(PNG_WIDTH_OFFSET)
    val height = bigEndianInt(PNG_WIDTH_OFFSET + Int.SIZE_BYTES)
    return width <= 1 || height <= 1
}

private fun AvatarImage.bigEndianInt(offset: Int): Int =
    (0 until Int.SIZE_BYTES).fold(0) { value, index ->
        (value shl Byte.SIZE_BITS) or (bytes[offset + index].toInt() and BYTE_MASK)
    }

private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte())
private const val PNG_WIDTH_OFFSET = 16
private const val PNG_IHDR_END = 24
private const val BYTE_MASK = 0xFF

internal suspend fun <T> nullOnFailure(load: suspend () -> T?): T? =
    runCatching { load() }.getOrElse {
        if (it is CancellationException) throw it
        null
    }
