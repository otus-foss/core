package com.otus.avatar

import com.otus.storage.storedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray

/**
 * App-private, bounded encoded-image cache. Records use hashed keys and atomic replacement. The
 * store reads every record's size and age once and keeps them in memory, so use one store per
 * directory.
 */
public class FileAvatarStore(
    directory: String,
    private val maxBytes: Long = DEFAULT_MAX_BYTES,
    private val maxEntries: Int = 256,
) : AvatarStore {
    private val directory = Path(directory)
    private val lock = Mutex()
    private val files = SystemFileSystem
    private var index: AvatarDiskIndex? = null

    init {
        require(maxBytes >= HEADER_BYTES && maxEntries > 0)
    }

    override suspend fun read(key: AvatarCacheKey): StoredAvatar? =
        access {
            val path = path(key)
            val size = files.metadataOrNull(path)?.size ?: return@access null
            if (size !in HEADER_BYTES..MAX_AVATAR_BYTES + HEADER_BYTES) {
                files.delete(path)
                index?.remove(path.name)
                return@access null
            }
            files.source(path).buffered().use { source ->
                decodedRecord(source.readLong(), source.readByte().toInt(), source.readByteArray())
            }
        }

    private fun decodedRecord(
        created: Long,
        kind: Int,
        bytes: ByteArray,
    ): StoredAvatar? =
        when {
            created < 0 -> null
            kind == MISS_RECORD && bytes.isEmpty() -> StoredAvatar(null, created)
            kind == IMAGE_RECORD && bytes.isNotEmpty() -> StoredAvatar(AvatarImage(bytes), created)
            kind == STYLE_RECORD -> decodedAvatarStyle(bytes.decodeToString())?.let { StoredAvatar(null, created, it) }
            else -> null
        }

    override suspend fun write(
        key: AvatarCacheKey,
        entry: StoredAvatar,
    ) {
        access {
            val bytes = entry.image?.bytes ?: entry.style?.encoded()?.encodeToByteArray()
            if (bytes != null && (bytes.isEmpty() || bytes.size > MAX_AVATAR_BYTES)) return@access
            val kind =
                when {
                    bytes == null -> MISS_RECORD
                    entry.image == null -> STYLE_RECORD
                    else -> IMAGE_RECORD
                }
            val disk = index()
            files.createDirectories(directory)
            val target = path(key)
            val temporary = Path(directory, "${target.name}.part")
            try {
                files.sink(temporary).buffered().use { sink ->
                    sink.writeLong(entry.created)
                    sink.writeByte(kind.toByte())
                    if (bytes != null) sink.write(bytes)
                }
                files.atomicMove(temporary, target)
                disk.put(target.name, HEADER_BYTES + (bytes?.size ?: 0), entry.created)
                disk.prune().forEach { files.delete(Path(directory, it), mustExist = false) }
            } finally {
                files.delete(temporary, mustExist = false)
            }
        }
    }

    override suspend fun clear() {
        access {
            records().forEach { files.delete(it) }
            index?.clear()
        }
    }

    override suspend fun removeAccount(account: String) {
        access {
            val prefix = "${storedName(account)}-"
            records().filter { it.name.startsWith(prefix) }.forEach { record ->
                files.delete(record)
                index?.remove(record.name)
            }
        }
    }

    private fun path(key: AvatarCacheKey): Path {
        val partition = key.account?.let(::storedName) ?: "public"
        return Path(directory, "$partition-${storedName(key.value)}.avatar")
    }

    private fun records(): List<Path> =
        if (files.exists(directory)) {
            files.list(directory).filter { it.name.endsWith(".avatar") || it.name.endsWith(".avatar.part") }
        } else {
            emptyList()
        }

    /** The index of the records on disk, read the first time a write needs it. */
    private fun index(): AvatarDiskIndex {
        val current = index
        if (current != null) return current
        // A process killed during a write can leave a temporary record behind.
        records().filter { it.name.endsWith(".part") }.forEach { files.delete(it) }
        val loaded = AvatarDiskIndex(maxBytes, maxEntries)
        records().forEach { path -> loaded.put(path.name, files.metadataOrNull(path)?.size ?: 0, created(path)) }
        index = loaded
        return loaded
    }

    /** The record's creation time, or 0 so that an unreadable record is pruned first. */
    private fun created(path: Path): Long =
        runCatching { files.source(path).buffered().use { it.readLong() } }.getOrDefault(0)

    private suspend fun <T> access(block: () -> T): T? =
        withContext(Dispatchers.IO) {
            lock.withLock { nullOnFailure { block() } }
        }

    private companion object {
        const val HEADER_BYTES = 9L
        const val MISS_RECORD = 0
        const val IMAGE_RECORD = 1
        const val STYLE_RECORD = 2
        const val DEFAULT_MAX_BYTES = 32L * 1024 * 1024
    }
}
