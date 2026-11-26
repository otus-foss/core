package com.otusfoss.core.storage

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.InvalidAlgorithmParameterException
import java.security.KeyStore
import java.security.ProviderException
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64

/** Wraps the storage key for keeping in preferences. Production uses the Android Keystore. */
internal interface KeyWrap {
    /** Throws [GeneralSecurityException] when the key cannot be wrapped. */
    fun wrap(key: ByteArray): Wrapped

    fun unwrap(wrapped: String): Unwrapped

    /** Deletes the wrapping key, so the next [wrap] makes a new one. */
    fun forget()
}

/** A wrapped key, and whether wrapping it made the Keystore key, as on the device's first start. */
internal class Wrapped(
    val text: String,
    val madeWrappingKey: Boolean,
)

/** The storage key and how long it lasts. */
internal class StorageKey(
    val key: SecretKey,
    val life: KeyLife,
)

internal sealed interface Unwrapped {
    class Key(
        val bytes: ByteArray,
    ) : Unwrapped

    /** The wrapping key is gone or no longer fits, so what the storage key sealed cannot be read again. */
    data object Lost : Unwrapped

    /** The Keystore did not answer this time; the saved key may still be good. */
    data object Failed : Unwrapped
}

/** The device's storage key, saved wrapped in [preferences]; see the overload that takes [WrappedKey]. */
internal fun storageKey(
    preferences: SharedPreferences,
    wrap: KeyWrap,
): StorageKey =
    storageKey(
        WrappedKey(
            read = { preferences.getString(WRAPPED_KEY, null) },
            write = { preferences.edit().putString(WRAPPED_KEY, it).commit() },
        ),
        wrap,
    )

/** Where the wrapped storage key is kept. */
internal class WrappedKey(
    val read: () -> String?,
    /** False when the key could not be saved. */
    val write: (String) -> Boolean,
)

/**
 * The device's storage key, kept wrapped in [saved]. A first use, or a key that is lost for good,
 * makes a new one. It is the device's [KeyLife.First] key only when the Keystore key is new too:
 * the app's files cannot add or remove a Keystore key, so deleting the saved key does not make a
 * start look like the first. While the Keystore fails, the key lasts for this process only and
 * the saved one is left alone, so what was sealed reads again once the Keystore answers. A lost
 * wrapping key is deleted and made again, since it never wraps again, and a wrapping key this start
 * made is deleted when the storage key cannot be saved, so the next start is the first again.
 */
internal fun storageKey(
    saved: WrappedKey,
    wrap: KeyWrap,
): StorageKey {
    val unwrapped = saved.read()?.let(wrap::unwrap)
    if (unwrapped is Unwrapped.Key) return StorageKey(SecretKeySpec(unwrapped.bytes, AES), KeyLife.Kept)
    val fresh = ByteArray(KEY_BYTES).also(SecureRandom()::nextBytes)
    val life =
        if (unwrapped is Unwrapped.Failed) {
            KeyLife.ProcessOnly
        } else {
            keep(
                fresh,
                saved,
                wrap,
                lost =
                    unwrapped != null,
            )
        }
    return StorageKey(SecretKeySpec(fresh, AES), life)
}

/** Saves [fresh] as the device's storage key, as the first or in place of a [lost] one, and says how long it lasts. */
private fun keep(
    fresh: ByteArray,
    saved: WrappedKey,
    wrap: KeyWrap,
    lost: Boolean,
): KeyLife {
    if (lost) wrap.forget()
    return try {
        val wrapped = wrap.wrap(fresh)
        when {
            // A key that was never saved opens nothing at the next start.
            !saved.write(wrapped.text) -> {
                if (wrapped.madeWrappingKey) wrap.forget()
                KeyLife.ProcessOnly
            }

            // A lost key's replacement is not the device's first key.
            wrapped.madeWrappingKey && !lost -> {
                KeyLife.First
            }

            else -> {
                KeyLife.Kept
            }
        }
    } catch (_: GeneralSecurityException) {
        // Without a Keystore the key cannot be kept, so it lasts for this process.
        KeyLife.ProcessOnly
    }
}

/** AES-256-GCM with a non-exportable Android Keystore key under [alias]. */
internal class KeystoreKeyWrap(
    private val alias: String,
) : KeyWrap {
    override fun wrap(key: ByteArray): Wrapped =
        try {
            val kept = keystoreKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, kept ?: generateKey())
            Wrapped(Base64.encode(cipher.iv) + SEPARATOR + Base64.encode(cipher.doFinal(key)), kept == null)
        } catch (error: ProviderException) {
            throw GeneralSecurityException(error)
        } catch (error: IOException) {
            throw GeneralSecurityException(error)
        }

    override fun unwrap(wrapped: String): Unwrapped =
        try {
            val parts = wrapped.split(SEPARATOR).takeIf { it.size == 2 }
            val key = parts?.let { keystoreKey() }
            if (parts == null || key == null) {
                Unwrapped.Lost
            } else {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, Base64.decode(parts[0])))
                Unwrapped.Key(cipher.doFinal(Base64.decode(parts[1])))
            }
        } catch (_: KeyPermanentlyInvalidatedException) {
            Unwrapped.Lost
        } catch (_: AEADBadTagException) {
            Unwrapped.Lost
        } catch (_: InvalidAlgorithmParameterException) {
            // The saved key's IV is broken, which no later start repairs.
            Unwrapped.Lost
        } catch (_: IllegalArgumentException) {
            Unwrapped.Lost
        } catch (_: GeneralSecurityException) {
            Unwrapped.Failed
        } catch (_: ProviderException) {
            // AndroidKeyStore reports some key failures outside GeneralSecurityException.
            Unwrapped.Failed
        } catch (_: IOException) {
            Unwrapped.Failed
        }

    override fun forget() {
        try {
            KeyStore
                .getInstance(KEYSTORE)
                .apply { load(null) }
                .deleteEntry(alias)
        } catch (_: GeneralSecurityException) {
            // The next wrap meets the old key again and fails, so that start lasts for its process.
        } catch (_: IOException) {
            // As above.
        } catch (_: ProviderException) {
            // As above.
        }
    }

    private fun keystoreKey(): SecretKey? =
        KeyStore
            .getInstance(KEYSTORE)
            .apply { load(null) }
            .getEntry(alias, null)
            .let { it as? KeyStore.SecretKeyEntry }
            ?.secretKey

    private fun generateKey(): SecretKey =
        KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
            .apply {
                init(
                    KeyGenParameterSpec
                        .Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(KEY_BYTES * Byte.SIZE_BITS)
                        .build(),
                )
            }.generateKey()

    private companion object {
        const val KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_BITS = 128
        const val SEPARATOR = ":"
    }
}

private const val WRAPPED_KEY = "key"
private const val KEY_BYTES = 32
private const val AES = "AES"
