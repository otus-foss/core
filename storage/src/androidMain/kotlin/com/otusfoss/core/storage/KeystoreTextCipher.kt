package com.otusfoss.core.storage

import android.content.Context
import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.io.encoding.Base64

/**
 * AES-256-GCM with the device's storage key. The key is random, and it is kept in app-private
 * preferences wrapped by a key that never leaves the Android Keystore, so each seal and open runs
 * in the process without a Keystore call. Each sealed text gets a fresh nonce.
 */
public class KeystoreTextCipher internal constructor(
    loadKey: () -> SecretKey,
) : TextCipher {
    public constructor(
        context: Context,
        preferencesName: String = "otus_storage_key",
        keyAlias: String = "otus.storage.key",
    ) : this({
        storageKey(
            context.applicationContext.getSharedPreferences(preferencesName, Context.MODE_PRIVATE),
            KeystoreKeyWrap(keyAlias),
        )
    })

    private val key by lazy(loadKey)
    private val random = SecureRandom()

    override fun seal(
        text: String,
        context: String,
    ): String {
        val nonce = ByteArray(NONCE_BYTES).also(random::nextBytes)
        val sealed = cipher(Cipher.ENCRYPT_MODE, nonce, context).doFinal(text.encodeToByteArray())
        return SEALED_PREFIX + Base64.encode(nonce + sealed)
    }

    override fun open(
        sealed: String,
        context: String,
    ): String? =
        try {
            sealed
                .takeIf(::isSealedText)
                ?.let { Base64.decode(it.substring(SEALED_PREFIX.length)) }
                ?.takeIf { it.size >= NONCE_BYTES }
                ?.let { bytes ->
                    cipher(Cipher.DECRYPT_MODE, bytes.copyOf(NONCE_BYTES), context)
                        .doFinal(bytes, NONCE_BYTES, bytes.size - NONCE_BYTES)
                        .decodeToString()
                }
        } catch (_: GeneralSecurityException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }

    private fun cipher(
        mode: Int,
        nonce: ByteArray,
        context: String,
    ): Cipher =
        Cipher.getInstance(TRANSFORMATION).apply {
            init(mode, key, GCMParameterSpec(TAG_BITS, nonce))
            updateAAD(context.encodeToByteArray())
        }

    private companion object {
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_BITS = 128
        const val NONCE_BYTES = 12
    }
}
