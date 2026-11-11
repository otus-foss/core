package com.otusfoss.core.avatar

import kotlin.time.Clock

/** Cache policy and storage can be replaced together for previews and tests. */
public class AvatarCacheConfig(
    public val maxEntries: Int = 64,
    public val cacheMillis: Long = DEFAULT_CACHE_MILLIS,
    public val nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() },
    public val store: AvatarStore = EmptyAvatarStore,
) {
    init {
        require(maxEntries > 0 && cacheMillis > 0)
    }

    private companion object {
        const val DEFAULT_CACHE_MILLIS = 30 * 24 * 60 * 60 * 1000L
    }
}

/** Public website icons have no account; contact photos remain private to one login. */
public data class AvatarCacheKey(
    val account: String?,
    val value: String,
)

/** A lookup result: an [image], a [style] from a contact card, or neither for a miss. */
public data class StoredAvatar(
    val image: AvatarImage?,
    val created: Long,
    val style: AvatarStyle? = null,
)

/** Optional encoded-image storage. Implementations bound disk usage and tolerate corrupt records. */
public interface AvatarStore {
    public suspend fun read(key: AvatarCacheKey): StoredAvatar?

    public suspend fun write(
        key: AvatarCacheKey,
        entry: StoredAvatar,
    )

    public suspend fun clear()

    public suspend fun removeAccount(account: String)
}

public object EmptyAvatarStore : AvatarStore {
    override suspend fun read(key: AvatarCacheKey): StoredAvatar? = null

    override suspend fun write(
        key: AvatarCacheKey,
        entry: StoredAvatar,
    ): Unit = Unit

    override suspend fun clear(): Unit = Unit

    override suspend fun removeAccount(account: String): Unit = Unit
}
