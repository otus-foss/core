package com.otus.designsystem.elements

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryGetValue
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFNumberCreate
import platform.CoreFoundation.CFNumberGetValue
import platform.CoreFoundation.CFNumberRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFNumberIntType
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.ImageIO.CGImageSourceCopyPropertiesAtIndex
import platform.ImageIO.CGImageSourceCreateThumbnailAtIndex
import platform.ImageIO.CGImageSourceCreateWithData
import platform.ImageIO.CGImageSourceRef
import platform.ImageIO.kCGImagePropertyPixelHeight
import platform.ImageIO.kCGImagePropertyPixelWidth
import platform.ImageIO.kCGImageSourceCreateThumbnailFromImageAlways
import platform.ImageIO.kCGImageSourceCreateThumbnailWithTransform
import platform.ImageIO.kCGImageSourceThumbnailMaxPixelSize

/** ImageIO thumbnails decode at [maxSize], so large photos never allocate a full-size bitmap. */
@OptIn(ExperimentalForeignApi::class)
public actual fun decodeImage(
    bytes: ByteArray,
    maxSize: Int,
): ImageBitmap? {
    val source = imageSource(bytes) ?: return null
    try {
        return if (hasAllowedSize(
                source,
            )
        ) {
            thumbnail(source, maxSize)?.let { rasterizeAndRelease(it, maxSize) }
        } else {
            null
        }
    } finally {
        CFRelease(source)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun imageSource(bytes: ByteArray): CGImageSourceRef? {
    if (bytes.isEmpty()) return null
    val data = bytes.usePinned { CFDataCreate(null, it.addressOf(0).reinterpret(), bytes.size.convert()) }
    return data?.let { CGImageSourceCreateWithData(it, null).also { _ -> CFRelease(it) } }
}

@OptIn(ExperimentalForeignApi::class)
private fun hasAllowedSize(source: CGImageSourceRef): Boolean {
    val properties = CGImageSourceCopyPropertiesAtIndex(source, 0u, null) ?: return false
    try {
        val width = properties.intValue(kCGImagePropertyPixelWidth)
        val height = properties.intValue(kCGImagePropertyPixelHeight)
        return width in 1..MAX_IMAGE_DIMENSION && height in 1..MAX_IMAGE_DIMENSION
    } finally {
        CFRelease(properties)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun CFDictionaryRef.intValue(key: CFStringRef?): Int? =
    memScoped {
        val number: CFNumberRef = CFDictionaryGetValue(this@intValue, key)?.reinterpret() ?: return null
        val result = alloc<IntVar>()
        if (CFNumberGetValue(number, kCFNumberIntType, result.ptr)) result.value else null
    }

@OptIn(ExperimentalForeignApi::class)
private fun thumbnail(
    source: CGImageSourceRef,
    maxPixels: Int,
): CGImageRef? =
    memScoped {
        val size = alloc<IntVar>().apply { value = maxPixels }
        val maxSize = CFNumberCreate(null, kCFNumberIntType, size.ptr)
        val options =
            CFDictionaryCreateMutable(
                null,
                THUMBNAIL_OPTION_COUNT,
                kCFTypeDictionaryKeyCallBacks.ptr,
                kCFTypeDictionaryValueCallBacks.ptr,
            )
        try {
            CFDictionarySetValue(options, kCGImageSourceCreateThumbnailFromImageAlways, kCFBooleanTrue)
            CFDictionarySetValue(options, kCGImageSourceCreateThumbnailWithTransform, kCFBooleanTrue)
            CFDictionarySetValue(options, kCGImageSourceThumbnailMaxPixelSize, maxSize)
            CGImageSourceCreateThumbnailAtIndex(source, 0u, options)
        } finally {
            CFRelease(options)
            CFRelease(maxSize)
        }
    }

@OptIn(ExperimentalForeignApi::class)
private fun rasterizeAndRelease(
    image: CGImageRef,
    maxSize: Int,
): ImageBitmap? =
    try {
        rasterize(image, maxSize)
    } finally {
        CGImageRelease(image)
    }

/** Draws into premultiplied RGBA memory that Skia can wrap directly. */
@OptIn(ExperimentalForeignApi::class)
private fun rasterize(
    image: CGImageRef,
    maxSize: Int,
): ImageBitmap? {
    val width = CGImageGetWidth(image).toInt()
    val height = CGImageGetHeight(image).toInt()
    if (width !in 1..maxSize || height !in 1..maxSize) return null
    val rowBytes = width * BYTES_PER_PIXEL
    val pixels = ByteArray(rowBytes * height)
    val info = ImageInfo(width, height, ColorType.RGBA_8888, ColorAlphaType.PREMUL)
    return if (draw(
            image,
            pixels,
            width,
            height,
        )
    ) {
        Image.makeRaster(info, pixels, rowBytes).toComposeImageBitmap()
    } else {
        null
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun draw(
    image: CGImageRef,
    pixels: ByteArray,
    width: Int,
    height: Int,
): Boolean {
    val colors = CGColorSpaceCreateDeviceRGB()
    try {
        return pixels.usePinned {
            val context =
                CGBitmapContextCreate(
                    it.addressOf(0),
                    width.convert(),
                    height.convert(),
                    BITS_PER_COMPONENT.convert(),
                    (width * BYTES_PER_PIXEL).convert(),
                    colors,
                    CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value,
                )
            context?.let { drawing ->
                CGContextDrawImage(drawing, CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()), image)
                CGContextRelease(drawing)
            } != null
        }
    } finally {
        CGColorSpaceRelease(colors)
    }
}

private const val MAX_IMAGE_DIMENSION = 4096
private const val BYTES_PER_PIXEL = 4
private const val BITS_PER_COMPONENT = 8
private const val THUMBNAIL_OPTION_COUNT = 3L
