package com.otus.jmap

/**
 * Streams a blob as [JmapClient.download] does, but without a size limit: each piece of up to
 * 64 KiB goes to [receive] as it arrives, so a caller can write a large file to disk without
 * holding it in memory. It makes the same https and trusted-origin checks and throws
 * [JmapException.BlobNotFound] for a 404. Returns the media type the server sent. An exception
 * from [receive], or cancelling the caller's coroutine, ends the transfer; the caller bounds what
 * it accepts.
 */
internal suspend fun JmapClient.downloadStream(
    session: JmapSession,
    auth: JmapAuth,
    accountId: String,
    blobId: String,
    mediaType: String,
    name: String = DEFAULT_DOWNLOAD_NAME,
    receive: suspend (ByteArray) -> Unit,
): String = JmapDownloads(http, urls, policy).stream(session, auth, accountId, blobId, mediaType, name, receive)

/**
 * Streams through [JmapClient.downloadStream] with this connection's session and credentials;
 * [receive] gets each piece as it arrives. A failure drops the session as [JmapConnection.download]
 * does.
 */
public suspend fun JmapConnection.downloadStream(
    accountId: String,
    blobId: String,
    mediaType: String,
    name: String = DEFAULT_DOWNLOAD_NAME,
    receive: suspend (ByteArray) -> Unit,
): String = withSession { client.downloadStream(it, auth, accountId, blobId, mediaType, name, receive) }
