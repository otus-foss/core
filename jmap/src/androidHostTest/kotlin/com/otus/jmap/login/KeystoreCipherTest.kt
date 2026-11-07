package com.otus.jmap.login

import android.content.Context
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.ProviderException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import javax.crypto.spec.SecretKeySpec
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE)
class KeystoreCipherTest {
    private val failures = listOf(ProviderException("key invalidated"), IOException("keystore unreadable"))
    private val login = JmapLogin("a", "https://example.org/session", "ada", "Ada")

    @Test
    fun keystoreProviderAndIoFailuresMakeTheSecretUnreadable() {
        failures.forEach { failure ->
            val secret = KeystoreCipher("alias") { throw failure }.open("aXY=:c2VhbGVk")
            assertSame(failure, assertIs<LoginSecret.Unreadable>(secret).cause)
        }
    }

    @Test
    fun keystoreProviderAndIoFailuresBecomeStoreExceptionsOnSave() {
        val preferences =
            RuntimeEnvironment.getApplication().getSharedPreferences("keystore-test", Context.MODE_PRIVATE)
        failures.forEach { failure ->
            val cipher = KeystoreCipher("alias") { throw failure }
            assertSame(failure, assertFailsWith<GeneralSecurityException> { cipher.seal("secret") }.cause)
            val store = AndroidLoginStore(preferences, cipher)
            assertFailsWith<LoginStoreException> { store.save(login, "secret") }
        }
    }

    @Test
    fun concurrentFirstUseLoadsTheKeyOnce() {
        val loads = AtomicInteger()
        val release = CountDownLatch(1)
        val cipher =
            KeystoreCipher("alias") {
                loads.incrementAndGet()
                release.await()
                SecretKeySpec(ByteArray(KEY_BYTES), "AES")
            }
        val workers = List(THREADS) { index -> thread { cipher.seal("secret $index") } }
        // Every worker stops in the key load or waits for the one that is in it; only then can one finish.
        while (workers.any { it.state !in STOPPED }) Thread.yield()
        release.countDown()
        workers.forEach(Thread::join)
        assertEquals(1, loads.get())
    }

    private companion object {
        const val THREADS = 4
        val STOPPED = setOf(Thread.State.BLOCKED, Thread.State.WAITING, Thread.State.TERMINATED)
        const val KEY_BYTES = 32
    }
}
