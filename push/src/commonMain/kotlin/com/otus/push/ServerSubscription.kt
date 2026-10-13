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
                id?.let { connection.call(PushSubscriptionSetMethod(destroy = listOf(it))) }
                null
            }

            id == null || record.subscribed != endpoint -> {
                create(endpoint, replacing = id)
            }

            record.verificationCode != null || expiresSoon(record) -> {
                upkeep(record, id, endpoint)
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
        return when {
            // The server deleted it, perhaps because it expired; start a new one.
            result.notUpdated[id]?.type == NOT_FOUND -> {
                create(endpoint, replacing = null)
            }

            id !in result.updated -> {
                record
            }

            else -> {
                record.copy(
                    verificationCode = null,
                    verified = record.verified || code != null,
                    expires = result.updated[id]?.stringOf("expires") ?: expires ?: record.expires,
                )
            }
        }
    }

    private suspend fun create(
        endpoint: PushEndpoint,
        replacing: String?,
    ): PushRecord {
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

    private fun expiresSoon(record: PushRecord): Boolean =
        record.expires?.let { Instant.parse(it) - clock.now() < RENEW_BEFORE } ?: false

    private fun expiry(): String = Instant.fromEpochSeconds((clock.now() + LIFETIME).epochSeconds).toString()

    private companion object {
        const val CREATION = "push"
        const val NOT_FOUND = "notFound"

        /** What a server without push subscriptions, or one that forbids them, answers. */
        val REFUSALS = setOf("unknownMethod", "forbidden")

        /** Servers may shorten it; [RENEW_BEFORE] leaves room for days without a sync. */
        val LIFETIME: Duration = 7.days
        val RENEW_BEFORE: Duration = 3.days
    }
}

private fun JsonObject.stringOf(key: String): String? = (get(key) as? JsonPrimitive)?.takeIf { it.isString }?.content
