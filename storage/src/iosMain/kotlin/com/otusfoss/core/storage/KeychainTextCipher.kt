package com.otusfoss.core.storage

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.value
import platform.CoreCrypto.CCCrypt
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreCrypto.kCCAlgorithmAES
import platform.CoreCrypto.kCCBlockSizeAES128
import platform.CoreCrypto.kCCDecrypt
import platform.CoreCrypto.kCCEncrypt
import platform.CoreCrypto.kCCHmacAlgSHA256
import platform.CoreCrypto.kCCOptionPKCS7Padding
import platform.CoreCrypto.kCCSuccess
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault
import kotlin.io.encoding.Base64

/**
 * AES-256-CBC with an HMAC-SHA-256 over the context, nonce, and sealed bytes, checked before
 * anything is decrypted. The two keys are random and kept as one Keychain item, on this device
 * only and readable after the first unlock. Each sealed text gets a fresh nonce.
 */
@OptIn(ExperimentalForeignApi::class)
public class KeychainTextCipher internal constructor(
    loadKeys: () -> ByteArray,
) : TextCipher {
    public constructor(service: String = "otus.storage.key") : this({ keychainStorageKeys(service) })

    private val keys by lazy(loadKeys)
    private val cipherKey get() = keys.copyOfRange(0, KEY_BYTES)
    private val macKey get() = keys.copyOfRange(KEY_BYTES, KEY_BYTES * 2)

    override fun seal(
        text: String,
        context: String,
    ): String {
        val nonce = randomBytes(NONCE_BYTES)
        val sealed = nonce + crypt(kCCEncrypt, nonce, text.encodeToByteArray())
        return SEALED_PREFIX + Base64.encode(sealed + mac(context, sealed))
    }

    override fun open(
        sealed: String,
        context: String,
    ): String? =
        try {
            sealed
                .takeIf(::isSealedText)
                ?.let { Base64.decode(it.substring(SEALED_PREFIX.length)) }
                ?.takeIf { it.size >= NONCE_BYTES + MAC_BYTES }
                ?.let { it.copyOfRange(0, it.size - MAC_BYTES) to it.copyOfRange(it.size - MAC_BYTES, it.size) }
                ?.takeIf { (body, sealedMac) -> sameBytes(mac(context, body), sealedMac) }
                ?.let { (body, _) -> body.copyOf(NONCE_BYTES) to body.copyOfRange(NONCE_BYTES, body.size) }
                ?.let { (nonce, encrypted) -> crypt(kCCDecrypt, nonce, encrypted) }
                ?.decodeToString()
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: IllegalStateException) {
            null
        }

    /** [input] encrypted or decrypted as [operation] says, with PKCS#7 padding. */
    private fun crypt(
        operation: UInt,
        nonce: ByteArray,
        input: ByteArray,
    ): ByteArray =
        memScoped {
            // refTo needs an element to point at, so an empty input borrows the output's block.
            val output = ByteArray(input.size + NONCE_BYTES)
            val moved = alloc<ULongVar>()
            val status =
                CCCrypt(
                    operation,
                    kCCAlgorithmAES,
                    kCCOptionPKCS7Padding,
                    cipherKey.refTo(0),
                    KEY_BYTES.toULong(),
                    nonce.refTo(0),
                    if (input.isEmpty()) output.refTo(0) else input.refTo(0),
                    input.size.toULong(),
                    output.refTo(0),
                    output.size.toULong(),
                    moved.ptr,
                )
            check(status == kCCSuccess) { "CCCrypt failed ($status)." }
            output.copyOf(moved.value.toInt())
        }

    /** Covers the context's length too, so no context and sealed bytes read as another pair. */
    private fun mac(
        context: String,
        sealed: ByteArray,
    ): ByteArray {
        val named = context.encodeToByteArray()
        val data = "${named.size}:".encodeToByteArray() + named + sealed
        val out = ByteArray(CC_SHA256_DIGEST_LENGTH)
        CCHmac(kCCHmacAlgSHA256, macKey.refTo(0), KEY_BYTES.toULong(), data.refTo(0), data.size.toULong(), out.refTo(0))
        return out
    }

    private companion object {
        const val KEY_BYTES = 32
        val NONCE_BYTES = kCCBlockSizeAES128.toInt()
        const val MAC_BYTES = CC_SHA256_DIGEST_LENGTH
    }
}

/** Compares every byte whatever the first difference, so timing tells nothing about the MAC. */
private fun sameBytes(
    one: ByteArray,
    other: ByteArray,
): Boolean =
    one.size == other.size &&
        one.indices.fold(0) { difference, at -> difference or (one[at].toInt() xor other[at].toInt()) } == 0

@OptIn(ExperimentalForeignApi::class)
internal fun randomBytes(count: Int): ByteArray =
    ByteArray(count).also { bytes ->
        check(SecRandomCopyBytes(kSecRandomDefault, count.toULong(), bytes.refTo(0)) == errSecSuccess) {
            "The device gave no random bytes."
        }
    }
