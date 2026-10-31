package com.otus.jmap.push

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
    private val beforeCreate: suspend () -> Unit = {},
) {
    /** The record after syncing, or null once the subscription is gone and nothing is wanted. */
    suspend fun synced(record: PushRecord): PushRecord? {
        val id = record.subscriptionId
        val endpoint = record.endpoint
        if (endpoint == null) {
            if (id != null) destroy(id)
            return null
        }
        // A server keeps a subscription's types, so other types need a new subscription.
        return when {
            id == null || record.subscribed != endpoint || record.types != types -> create(endpoint, replacing = id)
            record.verificationCode != null || expiresSoon(record) -> upkeep(record, id, endpoint)
            verificationLapsed(record) -> create(endpoint, replacing = id)
            else -> record
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
                if (code != null) put("verificationCode", JsonPrimitive(code))
                if (expires != null) put("expires", JsonPrimitive(expires))
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
            if (replacing != null) destroy(replacing)
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
                types = types,
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
            beforeCreate()
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

    /** The verification push never arrived, and the server pushes nothing until it does. */
    private fun verificationLapsed(record: PushRecord): Boolean = !record.verified && age(record) > VERIFY_WITHIN

    private fun age(record: PushRecord): Duration =
        record.since?.let(Instant::parseOrNull)?.let { clock.now() - it } ?: Duration.INFINITE

    private fun now(): String = utcDate(clock.now())

    private fun expiry(): String = utcDate(clock.now() + LIFETIME)

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

/** A UTCDate in whole seconds, such as `2026-10-14T08:00:00Z`. */
private fun utcDate(instant: Instant): String = Instant.fromEpochSeconds(instant.epochSeconds).toString()

private fun JsonObject.stringOf(key: String): String? = (get(key) as? JsonPrimitive)?.takeIf { it.isString }?.content
