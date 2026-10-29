package com.otus.avatar

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Coalesces fetches while serializing persistent IO and invalidation separately from metadata. */
internal class AvatarLookupCache(
    private val store: AvatarStore,
    private val capacity: Int,
    private val lifetime: Long,
    private val now: () -> Long,
) {
    private val lock = Mutex()
    private val storageLock = Mutex()
    private val entries = linkedMapOf<AvatarCacheKey, StoredAvatar>()
    private val pending = mutableMapOf<AvatarCacheKey, Lookup>()
    private var generation = 0L

    suspend fun begin(): Long = lock.withLock { generation }

    suspend fun peek(key: AvatarCacheKey): StoredAvatar? = storageLock.withLock { peekStored(key) }

    suspend fun load(
        key: AvatarCacheKey,
        started: Long,
        fetch: suspend () -> StoredAvatar?,
    ): StoredAvatar {
        while (true) {
            var owner = false
            val lookup =
                lock.withLock {
                    recent(key)?.takeIf { it.isFresh(now(), lifetime) }?.let { return it }
                    if (started != generation) {
                        owner = true
                        Lookup(started)
                    } else {
                        pending.getOrPut(key) {
                            owner = true
                            Lookup(started)
                        }
                    }
                }
            val result = if (owner) fetchShared(key, lookup, fetch) else lookup.result.await()
            if (result != null) return result
        }
    }

    suspend fun clear(account: String? = null) {
        storageLock.withLock {
            lock.withLock {
                generation++
                if (account == null) {
                    entries.clear()
                    pending.clear()
                } else {
                    entries.keys.removeAll { it.account == account }
                    pending.keys.removeAll { it.account == account }
                }
            }
            if (account == null) store.clear() else store.removeAccount(account)
        }
    }

    /**
     * Stores [entry] for [key] as current, such as an avatar the user just saved. Lookups that
     * started earlier can no longer overwrite it.
     */
    suspend fun put(
        key: AvatarCacheKey,
        entry: StoredAvatar,
    ) {
        storageLock.withLock {
            val current = entry.copy(created = now())
            lock.withLock {
                generation++
                pending.remove(key)
                remember(key, current)
            }
            store.write(key, current)
        }
    }

    /**
     * Makes the next lookup of [key] fetch again, and stops lookups that started earlier from
     * storing their result. The stored record stays as a stale miss rather than an old image, so a
     * removed photo is not brought back when the fetch finds nothing.
     */
    suspend fun expire(key: AvatarCacheKey) {
        storageLock.withLock {
            lock.withLock {
                generation++
                pending.remove(key)
                entries.remove(key)
            }
            store.write(key, StoredAvatar(null, EXPIRED))
        }
    }

    private suspend fun fetchShared(
        key: AvatarCacheKey,
        lookup: Lookup,
        fetch: suspend () -> StoredAvatar?,
    ): StoredAvatar {
        var result: StoredAvatar? = null
        try {
            val saved =
                storageLock.withLock {
                    if (lock.withLock { generation == lookup.generation }) peekStored(key) else null
                }
            if (saved != null && saved.isFresh(now(), lifetime)) {
                result = saved
                return saved
            }
            // A failed or empty fetch keeps the stale result rather than dropping a known avatar.
            val entry = (fetch() ?: saved)?.copy(created = now()) ?: StoredAvatar(null, now())
            storageLock.withLock {
                val current =
                    lock.withLock {
                        val current = generation == lookup.generation
                        if (current) remember(key, entry)
                        current
                    }
                if (current) store.write(key, entry)
            }
            result = entry
            return entry
        } finally {
            withContext(NonCancellable) {
                lock.withLock { if (pending[key] === lookup) pending.remove(key) }
            }
            lookup.result.complete(result)
        }
    }

    /** Storage lock held; metadata lock never covers disk IO or network work. */
    private suspend fun peekStored(key: AvatarCacheKey): StoredAvatar? {
        lock.withLock { recent(key) }?.let { return it }
        val stored = store.read(key)
        if (stored != null) lock.withLock { remember(key, stored) }
        return stored
    }

    /** The entry for [key], moved to the end so the least recently used one is evicted first. */
    private fun recent(key: AvatarCacheKey): StoredAvatar? = entries.remove(key)?.also { entries[key] = it }

    private fun remember(
        key: AvatarCacheKey,
        entry: StoredAvatar,
    ) {
        entries.remove(key)
        entries[key] = entry
        while (entries.size > capacity) entries.remove(entries.keys.first())
    }

    private class Lookup(
        val generation: Long,
        val result: CompletableDeferred<StoredAvatar?> = CompletableDeferred(),
    )

    private companion object {
        /** A creation time old enough that the record never counts as fresh. */
        const val EXPIRED = 0L
    }
}

/** Whether this entry is younger than [lifetime] at [now], or within five minutes for a miss. */
private fun StoredAvatar.isFresh(
    now: Long,
    lifetime: Long,
): Boolean {
    val age = now - created
    val ttl = if (image == null && style == null) minOf(lifetime, MISS_LIFETIME) else lifetime
    return age >= 0 && age < ttl
}

private const val MISS_LIFETIME = 5 * 60 * 1000L
