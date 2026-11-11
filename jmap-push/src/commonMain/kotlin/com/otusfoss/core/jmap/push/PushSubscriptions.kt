package com.otusfoss.core.jmap.push

import com.otusfoss.core.jmap.JmapConnections
import com.otusfoss.core.jmap.JmapException
import com.otusfoss.core.jmap.PushMessage
import com.otusfoss.core.jmap.login.JmapLogin
import com.otusfoss.core.jmap.login.LoginStoreException
import com.otusfoss.core.storage.StringMapStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Keeps one JMAP push subscription (RFC 8620, section 7.2) per login, pointed at the endpoint the
 * app's push service gave that login. Changes are saved at once; [sync] makes the server match.
 * Create one per process, so its lock covers every caller.
 *
 * [deviceClientId] identifies this app on this device to the server. It lets [sync] delete
 * subscriptions this app made before it lost its saved state, such as after a reinstall.
 * [types] are the data type names to push, such as `EmailDelivery`.
 *
 * [store] keeps each login's push record, keyed by login id. It holds no account credentials, but
 * it does hold each endpoint's Web Push keys, including the `auth` secret, with which anyone who
 * knows the endpoint URL can send pushes the app accepts. Keep it in app-private storage that is
 * excluded from backups.
 */
public class PushSubscriptions(
    private val connections: JmapConnections,
    private val store: StringMapStore,
    private val deviceClientId: String,
    private val types: List<String>,
    private val clock: Clock = Clock.System,
    /**
     * Pauses alert delivery before a new subscription sends its verification message. When it
     * throws, that login creates nothing and is tried again on the next [sync].
     */
    private val beforeCreate: suspend (loginId: String) -> Unit = {},
) {
    private val lock = Mutex()

    /**
     * Each login's push status, keyed by login id. Logins without a record are [PushStatus.Off].
     * It reads the last saved records without waiting for a running [sync].
     */
    public suspend fun status(): Map<String, PushStatus> =
        withContext(Dispatchers.IO) { store.records() }.mapValues { it.value.status }

    /** Saves where pushes for [loginId] should go. Call [sync] to tell the server. */
    public suspend fun setEndpoint(
        loginId: String,
        endpoint: PushEndpoint,
    ): Unit = change(loginId) { it.copy(endpoint = endpoint, refused = false) }

    /** Stops pushes for [loginId]. Call [sync] to delete the server's subscription. */
    public suspend fun clearEndpoint(loginId: String): Unit = change(loginId) { it.copy(endpoint = null) }

    /** Keeps the code of a verification push for [loginId], if it is for the current subscription. */
    public suspend fun receive(
        loginId: String,
        verification: PushMessage.Verification,
    ): Unit =
        change(loginId) { record ->
            record
                .takeIf { it.subscriptionId == verification.pushSubscriptionId }
                ?.copy(verificationCode = verification.verificationCode)
                ?: record
        }

    /**
     * Creates, replaces, verifies, renews, or deletes each login's subscription so the server
     * matches the saved endpoints. Servers are asked in parallel. A login whose server cannot be
     * reached, whose credentials cannot be read, or whose [beforeCreate] fails keeps its record and
     * is tried again on the next sync, while the other logins sync as usual. When the saved logins
     * cannot be read, nothing changes. Records of removed logins are dropped; call [forget] before
     * removing a login to delete its subscription too.
     */
    public suspend fun sync(): Unit =
        lock.withLock {
            val saved = withContext(Dispatchers.IO) { store.records() }
            val current = savedLogins() ?: return@withLock
            val next =
                coroutineScope {
                    current
                        .mapNotNull { login -> saved[login.id]?.let { login to it } }
                        .map { (login, record) -> async { synced(login, record)?.let { login.id to it } } }
                        .awaitAll()
                }
            withContext(Dispatchers.IO) { store.save(next.filterNotNull().toMap()) }
        }

    /**
     * Deletes [loginId]'s subscription on its server and drops its record. Call it while the
     * login's credentials still exist. It gives up after ten seconds, including time spent
     * waiting for a running [sync], so removing an account never hangs on a slow server. A server
     * that is not reached keeps the subscription until it expires, and the next [sync] drops the
     * record once the login is gone.
     */
    public suspend fun forget(loginId: String) {
        // On a real dispatcher, so the limit is wall-clock time even under a virtual-time scheduler.
        withContext(Dispatchers.Default) { forgetWithin(loginId) }
    }

    private suspend fun forgetWithin(loginId: String) {
        withTimeoutOrNull(FORGET_WITHIN) {
            lock.withLock {
                val saved = withContext(Dispatchers.IO) { store.records() }
                val record = saved[loginId] ?: return@withLock
                val login = savedLogins()?.find { it.id == loginId }
                if (login != null) synced(login, record.copy(endpoint = null))
                withContext(Dispatchers.IO) { store.save(saved - loginId) }
            }
        }
    }

    private suspend fun synced(
        login: JmapLogin,
        record: PushRecord,
    ): PushRecord? =
        try {
            ServerSubscription(connections.connection(login), deviceClientId, types, clock) {
                pauseOrSkip(beforeCreate, login.id)
            }.synced(record)
        } catch (_: JmapException) {
            record
        } catch (_: PauseFailed) {
            record
        } catch (_: LoginStoreException) {
            // Reading the secret failed; try again on the next sync.
            record
        }

    /** Null when storage cannot be read, so neither a sync nor removing an account fails here. */
    private suspend fun savedLogins(): List<JmapLogin>? =
        try {
            connections.savedLogins()
        } catch (_: LoginStoreException) {
            null
        }

    private suspend fun change(
        loginId: String,
        update: (PushRecord) -> PushRecord,
    ): Unit =
        lock.withLock {
            withContext(Dispatchers.IO) {
                val records = store.records()
                val next = records + (loginId to update(records[loginId] ?: PushRecord()))
                if (next != records) store.save(next)
            }
        }

    private companion object {
        /** How long [forget] may take before the account is removed without reaching its server. */
        val FORGET_WITHIN: Duration = 10.seconds
    }
}

/** Runs [beforeCreate], so that its failure skips only this login instead of the whole sync. */
private suspend fun pauseOrSkip(
    beforeCreate: suspend (String) -> Unit,
    loginId: String,
) {
    runCatching { beforeCreate(loginId) }.onFailure { error ->
        if (error is CancellationException) throw error
        throw PauseFailed(error)
    }
}

private class PauseFailed(
    cause: Throwable,
) : Exception(cause)
