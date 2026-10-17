package com.otus.push

import com.otus.jmap.JmapConnection
import com.otus.jmap.JmapException
import com.otus.jmap.NewPushSubscription
import com.otus.jmap.PushSubscriptionGetMethod
import com.otus.jmap.PushSubscriptionSetMethod
import com.otus.jmap.SetResult
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/** The server calls that bring one login's subscription in line with its [PushRecord]. */
internal class ServerSubscription(
    private val connection: JmapConnection,
    private val deviceClientId: String,
    private val types: List<String>,
    private val clock: Clock,
) {
    /** The record after syncing, or null once the subscription is gone and nothing is wanted. */
    suspend fun synced(record: PushRecord): PushRecord? {
        val endpoint = record.endpoint
        val id = record.subscriptionId
        return when {
            endpoint == null -> {
                id?.let { destroy(it) }
                null
            }

            id == null || record.subscribed != endpoint -> {
                create(endpoint, replacing = id)
            }

            record.verificationCode != null || expiresSoon(record) -> {
                upkeep(record, id, endpoint)
            }

            // The verification push never arrived, and the server pushes nothing until it does.
            !record.verified && age(record) > VERIFY_WITHIN -> {
                create(endpoint, replacing = id)
            }

            else -> {
                record
            }
        }
    }

    /** Sends a received verification code and extends an expiring subscription. */
    private suspend fun upkeep(
        record: PushRecord,
        id: String,
        endpoint: PushEndpoint,
    ): PushRecord {
        val code = record.verificationCode
        val expires = if (expiresSoon(record)) expiry() else null
        val patch =
            buildJsonObject {
                code?.let { put("verificationCode", JsonPrimitive(it)) }
                expires?.let { put("expires", JsonPrimitive(it)) }
            }
        val result = connection.call(PushSubscriptionSetMethod(update = mapOf(id to patch)))
        return if (id in result.notUpdated) {
            // The server deleted it or refused the change; a new one gets a new verification push.
            create(endpoint, replacing = null)
        } else {
            record.copy(
                verificationCode = null,
                verified = record.verified || code != null,
                expires = result.updated[id]?.stringOf("expires") ?: expires ?: record.expires,
                since = if (expires != null) now() else record.since,
            )
        }
    }

    private suspend fun create(
        endpoint: PushEndpoint,
        replacing: String?,
    ): PushRecord {
        // Without keys the server would POST readable state changes through the push provider,
        // and anyone who learns the URL could send pushes the app cannot tell apart from real ones.
        if (endpoint.keys == null || !NewPushSubscription.allows(endpoint.url)) {
            replacing?.let { destroy(it) }
            return PushRecord(endpoint, refused = true)
        }
        val subscription = NewPushSubscription(deviceClientId, endpoint.url, endpoint.keys, expiry(), types)
        val result = createReplacing(subscription, replacing)
        val id = result?.createdId(CREATION)
        return if (id == null) {
            PushRecord(endpoint, refused = true)
        } else {
            PushRecord(
                endpoint = endpoint,
                subscriptionId = id,
                subscribed = endpoint,
                expires = result.created[CREATION]?.stringOf("expires") ?: subscription.expires,
                since = now(),
            )
        }
    }

    /**
     * Creates [subscription] and deletes [replacing] and any earlier subscription of this device,
     * so the server sends each push once. Null when the server has no push subscriptions.
     */
    private suspend fun createReplacing(
        subscription: NewPushSubscription,
        replacing: String?,
    ): SetResult? =
        try {
            val stale =
                connection
                    .call(PushSubscriptionGetMethod())
                    .list
                    .filter { it.deviceClientId == deviceClientId }
                    .map { it.id } + listOfNotNull(replacing)
            connection.call(
                PushSubscriptionSetMethod(create = mapOf(CREATION to subscription), destroy = stale.distinct()),
            )
        } catch (error: JmapException.MethodError) {
            if (error.type in REFUSALS) null else throw error
        }

    private suspend fun destroy(id: String) {
        connection.call(PushSubscriptionSetMethod(destroy = listOf(id)))
    }

    /** Past half of the granted lifetime, so a server that grants less than asked is still renewed in time. */
    private fun expiresSoon(record: PushRecord): Boolean {
        val text = record.expires ?: return false
        val expires = Instant.parseOrNull(text)
        val since = record.since?.let(Instant::parseOrNull) ?: expires?.minus(LIFETIME)
        // Renewing replaces a date that cannot be read.
        return expires == null || since == null || clock.now() - since > (expires - since) / 2
    }

    private fun age(record: PushRecord): Duration =
        record.since?.let(Instant::parseOrNull)?.let { clock.now() - it } ?: Duration.INFINITE

    private fun now(): String = Instant.fromEpochSeconds(clock.now().epochSeconds).toString()

    private fun expiry(): String = Instant.fromEpochSeconds((clock.now() + LIFETIME).epochSeconds).toString()

    private companion object {
        const val CREATION = "push"

        /** What a server without push subscriptions, or one that forbids them, answers. */
        val REFUSALS = setOf("unknownMethod", "forbidden")

        /** Servers may shorten it. */
        val LIFETIME: Duration = 7.days

        /** Servers send the verification push at once; this leaves room for a push app that was offline. */
        val VERIFY_WITHIN: Duration = 1.hours
    }
}

private fun JsonObject.stringOf(key: String): String? = (get(key) as? JsonPrimitive)?.takeIf { it.isString }?.content
