package com.otusfoss.core.jmap

import com.otusfoss.core.jmap.login.JmapLogin
import com.otusfoss.core.jmap.login.LoginSecret
import com.otusfoss.core.jmap.login.LoginStore
import com.otusfoss.core.jmap.login.LoginStoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlin.coroutines.CoroutineContext

/**
 * One shared [JmapConnection] per saved login. Call [forget] after a login is replaced or removed.
 * Saved logins and their secrets are read on [io], off the main thread.
 */
public class JmapConnections(
    private val client: JmapClient,
    private val logins: LoginStore,
    private val io: CoroutineContext = Dispatchers.IO,
) {
    private val lock = Mutex()
    private val open = mutableMapOf<String, JmapConnection>()

    /** Throws [JmapException.CredentialsUnavailable] when the saved secret is missing or unreadable. */
    public suspend fun connection(login: JmapLogin): JmapConnection =
        lock.withLock {
            val current = open[login.id]
            if (current != null) return@withLock current
            val connection = create(login)
            open[login.id] = connection
            connection
        }

    /**
     * The account of [login] that offers [capability], with its connection and session. Throws
     * [JmapException.Unsupported] when no account of the login offers it.
     */
    public suspend fun account(
        login: JmapLogin,
        capability: String,
    ): JmapLoginAccount {
        val connection = connection(login)
        val session = connection.session()
        val id = session.accountFor(capability) ?: throw JmapException.Unsupported(capability)
        return JmapLoginAccount(login, connection, session, id)
    }

    /** [account] for the saved login [loginId], or null once that login was removed. */
    public suspend fun account(
        loginId: String,
        capability: String,
    ): JmapLoginAccount? = savedLogins().firstOrNull { it.id == loginId }?.let { account(it, capability) }

    /** The saved logins. Throws [LoginStoreException] when they cannot be read. */
    public suspend fun savedLogins(): List<JmapLogin> = withContext(io) { logins.logins() }

    public suspend fun forget(loginId: String) {
        lock.withLock { open.remove(loginId) }
    }

    private suspend fun create(login: JmapLogin): JmapConnection {
        val secret = withContext(io) { logins.secret(login.id) }
        if (secret !is LoginSecret.Found) throw JmapException.CredentialsUnavailable(login.id)
        return JmapConnection(client, login.sessionUrl, login.auth(secret.value), login.trustedOrigins)
    }
}

/** One saved login's connection, its session, and the [id] of its account for one capability. */
public class JmapLoginAccount(
    public val login: JmapLogin,
    public val connection: JmapConnection,
    public val session: JmapSession,
    public val id: String,
) {
    /** This account's object for [capability] read as [serializer]; see [JmapSession.accountCapability]. */
    public fun <T> capability(
        capability: String,
        serializer: KSerializer<T>,
    ): T? = session.accountCapability(id, capability, serializer)
}
