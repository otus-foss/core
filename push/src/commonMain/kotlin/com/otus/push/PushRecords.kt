package com.otus.push

import com.otus.jmap.JmapJson
import com.otus.jmap.PushKeys
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

/** Where a server should POST pushes for one login: a Web Push URL and its encryption keys. */
@Serializable
public data class PushEndpoint(
    val url: String,
    val keys: PushKeys?,
)

/** Whether pushes reach a login. */
public enum class PushStatus {
    /** The login has no push endpoint. */
    Off,

    /** The subscription is being created, or waits for the server's verification push. */
    Waiting,

    /** The server verified the subscription, so it pushes changes. */
    Active,

    /** The server refused the subscription; changes arrive only through periodic checks. */
    Refused,
}

/**
 * Each login's push record as text, keyed by login id. It holds no account credentials, but it
 * does hold each endpoint's Web Push keys, including the `auth` secret, with which anyone who
 * knows the endpoint URL can send pushes the app accepts. Apps keep it in app-private storage
 * that is excluded from backups.
 */
public interface PushStateStore {
    public fun read(): Map<String, String>

    public fun write(records: Map<String, String>)
}

/** What the app wants ([endpoint]) and what the server has ([subscribed]) for one login. */
@Serializable
internal data class PushRecord(
    val endpoint: PushEndpoint? = null,
    val subscriptionId: String? = null,
    val subscribed: PushEndpoint? = null,
    /** The UTCDate the server deletes the subscription. */
    val expires: String? = null,
    /** The UTCDate the subscription was created or last renewed. */
    val since: String? = null,
    /** A verification code received but not yet sent back. */
    val verificationCode: String? = null,
    val verified: Boolean = false,
    val refused: Boolean = false,
) {
    val status: PushStatus
        get() =
            when {
                endpoint == null -> PushStatus.Off
                refused -> PushStatus.Refused
                verified && subscribed == endpoint -> PushStatus.Active
                else -> PushStatus.Waiting
            }
}

/** Records that cannot be read are dropped; the next sync starts those logins again. */
internal fun PushStateStore.records(): Map<String, PushRecord> =
    read().mapNotNull { (login, text) -> decode(text)?.let { login to it } }.toMap()

private fun decode(text: String): PushRecord? =
    try {
        JmapJson.decodeFromString(PushRecord.serializer(), text)
    } catch (_: SerializationException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

internal fun PushStateStore.save(records: Map<String, PushRecord>) =
    write(records.mapValues { JmapJson.encodeToString(PushRecord.serializer(), it.value) })
