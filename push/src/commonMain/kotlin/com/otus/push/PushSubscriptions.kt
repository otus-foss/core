package com.otus.push

import com.otus.jmap.JmapConnections
import com.otus.jmap.JmapException
import com.otus.jmap.PushMessage
import com.otus.jmap.login.JmapLogin
import com.otus.jmap.login.LoginStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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

    /** Each login's push status, keyed by login id. Logins without a record are [PushStatus.Off]. */
    public suspend fun status(): Map<String, PushStatus> =
        edit { records ->
            records to
                records.mapValues { it.value.status }
        }

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
        change(loginId) {
            if (it.subscriptionId ==
                verification.pushSubscriptionId
            ) {
                it.copy(verificationCode = verification.verificationCode)
            } else {
                it
            }
        }

    /**
     * Creates, replaces, verifies, renews, or deletes each login's subscription so the server
     * matches the saved endpoints. A login whose server cannot be reached keeps its record and is
     * tried again on the next sync. Records of removed logins are dropped.
     */
    public suspend fun sync(): Unit =
        lock.withLock {
            val saved = withContext(Dispatchers.IO) { store.records() }
            val current = withContext(Dispatchers.IO) { logins.logins() }
            val next =
                current
                    .mapNotNull { login -> saved[login.id]?.let { login to it } }
                    .mapNotNull { (login, record) -> synced(login, record)?.let { login.id to it } }
            withContext(Dispatchers.IO) { store.save(next.toMap()) }
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

    private suspend fun change(
        loginId: String,
        update: (PushRecord) -> PushRecord,
    ) = edit { records -> records + (loginId to update(records[loginId] ?: PushRecord())) to Unit }

    private suspend fun <T> edit(block: (Map<String, PushRecord>) -> Pair<Map<String, PushRecord>, T>): T =
        lock.withLock {
            withContext(Dispatchers.IO) {
                val records = store.records()
                val (next, result) = block(records)
                if (next != records) store.save(next)
                result
            }
        }
}
