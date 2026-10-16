package com.otus.push

import com.otus.jmap.JmapConnections
import com.otus.jmap.JmapException
import com.otus.jmap.PushMessage
import com.otus.jmap.login.JmapLogin
import com.otus.jmap.login.LoginStore
import com.otus.jmap.login.LoginStoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Clock

/**
 * Keeps one JMAP push subscription (RFC 8620, section 7.2) per login, pointed at the endpoint the
 * app's push service gave that login. Changes are saved at once; [sync] makes the server match.
 * Create one per process, so its lock covers every caller.
 *
 * [deviceClientId] identifies this app on this device to the server. It lets [sync] delete
 * subscriptions this app made before it lost its saved state, such as after a reinstall.
 * [types] are the data type names to push, such as `EmailDelivery`.
 */
public class PushSubscriptions(
    private val connections: JmapConnections,
    private val logins: LoginStore,
    private val store: PushStateStore,
    private val deviceClientId: String,
    private val types: List<String>,
    private val clock: Clock = Clock.System,
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
     * reached keeps its record and is tried again on the next sync. Records of removed logins are
     * dropped; call [forget] before removing a login to delete its subscription too.
     */
    public suspend fun sync(): Unit =
        lock.withLock {
            val saved = withContext(Dispatchers.IO) { store.records() }
            val current = withContext(Dispatchers.IO) { logins.logins() }
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
     * login's credentials still exist. An unreachable server keeps the subscription until it expires.
     */
    public suspend fun forget(loginId: String): Unit =
        lock.withLock {
            val saved = withContext(Dispatchers.IO) { store.records() }
            val record = saved[loginId] ?: return@withLock
            loginOrNull(loginId)?.let { synced(it, record.copy(endpoint = null)) }
            withContext(Dispatchers.IO) { store.save(saved - loginId) }
        }

    private suspend fun synced(
        login: JmapLogin,
        record: PushRecord,
    ): PushRecord? =
        try {
            ServerSubscription(connections.connection(login), deviceClientId, types, clock).synced(record)
        } catch (_: JmapException) {
            record
        }

    /** Null when storage cannot be read, so removing an account never fails here. */
    private suspend fun loginOrNull(loginId: String): JmapLogin? =
        withContext(Dispatchers.IO) {
            try {
                logins.logins().find { it.id == loginId }
            } catch (_: LoginStoreException) {
                null
            }
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
}
