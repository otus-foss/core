package com.otus.jmap.login

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** AES-256-GCM with a non-exportable Android Keystore key. Each value gets a fresh provider-chosen IV. */
internal class KeystoreCipher(
    private val keyAlias: String,
    private val loadKey: () -> SecretKey = { keystoreKey(keyAlias) },
) : LoginCipher {
    @Volatile private var key: SecretKey? = null

    /** Throws [GeneralSecurityException] when the Keystore refuses, including provider and I/O failures. */
    override fun seal(value: String): String =
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
            val sealed = cipher.doFinal(value.encodeToByteArray())
            encode(cipher.iv) + SEPARATOR + encode(sealed)
        } catch (error: ProviderException) {
            throw GeneralSecurityException(error)
        } catch (error: IOException) {
            throw GeneralSecurityException(error)
        }

    override fun open(value: String): LoginSecret {
        val parts = value.split(SEPARATOR)
        if (parts.size != 2) return LoginSecret.Unreadable()
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, decode(parts[0])))
            LoginSecret.Found(cipher.doFinal(decode(parts[1])).decodeToString())
        } catch (error: GeneralSecurityException) {
            LoginSecret.Unreadable(error)
        } catch (error: ProviderException) {
            // AndroidKeyStore reports some key failures outside GeneralSecurityException.
            LoginSecret.Unreadable(error)
        } catch (error: IOException) {
            // KeyStore.load reads the keystore.
            LoginSecret.Unreadable(error)
        } catch (error: IllegalArgumentException) {
            // Malformed Base64 in storage.
            LoginSecret.Unreadable(error)
        }
    }

    /** Synchronized so that two first uses cannot both generate, and replace, the key. */
    private fun key(): SecretKey = key ?: synchronized(this) { key ?: loadAndCacheKey() }

    private fun loadAndCacheKey(): SecretKey {
        val loaded = loadKey()
        key = loaded
        return loaded
    }

    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(text: String) = Base64.decode(text, Base64.NO_WRAP)

    private companion object {
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_BITS = 128
        const val SEPARATOR = ":"
    }
}

private const val KEYSTORE = "AndroidKeyStore"
private const val KEY_BITS = 256

private fun keystoreKey(keyAlias: String): SecretKey =
    KeyStore
        .getInstance(KEYSTORE)
        .apply { load(null) }
        .getEntry(keyAlias, null)
        .let { it as? KeyStore.SecretKeyEntry }
        ?.secretKey
        ?: generateKey(keyAlias)

private fun generateKey(keyAlias: String): SecretKey =
    KeyGenerator
        .getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        .apply {
            init(
                KeyGenParameterSpec
                    .Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_BITS)
                    .build(),
            )
        }.generateKey()

/** Encryption boundary for platform storage tests. Production uses the Android Keystore. */
internal interface LoginCipher {
    fun seal(value: String): String

    fun open(value: String): LoginSecret
}
