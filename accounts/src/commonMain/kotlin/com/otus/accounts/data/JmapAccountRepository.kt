package com.otus.accounts.data

import com.otus.accounts.domain.AccountRepository
import com.otus.accounts.domain.AccountService
import com.otus.accounts.domain.AccountStorageException
import com.otus.accounts.domain.ConnectFailure
import com.otus.accounts.domain.ConnectOutcome
import com.otus.accounts.domain.ConnectedAccount
import com.otus.accounts.domain.JmapAccountDraft
import com.otus.accounts.domain.isValidUsername
import com.otus.jmap.GetMethod
import com.otus.jmap.JmapAuth
import com.otus.jmap.JmapClient
import com.otus.jmap.JmapConnections
import com.otus.jmap.JmapException
import com.otus.jmap.JmapSession
import com.otus.jmap.JmapType
import com.otus.jmap.login.JmapLogin
import com.otus.jmap.login.LoginStore
import com.otus.jmap.login.LoginStoreException
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject

/**
 * Verifies a server, credentials, and the app's [service], then saves the login. Nothing is saved
 * when verification fails.
 */
public class JmapAccountRepository(
    private val client: JmapClient,
    private val connections: JmapConnections,
    private val logins: LoginStore,
    private val service: AccountService,
    private val onAccountChanged: suspend (String) -> Unit = {},
) : AccountRepository {
    override suspend fun accounts(): List<ConnectedAccount> =
        storage { logins.logins() }.map { login ->
            login.toConnected().copy(emailAddresses = identityAddresses(login, service, connections))
        }

    override suspend fun connect(draft: JmapAccountDraft): ConnectOutcome {
        if (!isValidUsername(draft.username)) return ConnectOutcome.Failed(ConnectFailure.InvalidUsername)
        return try {
            val auth = JmapAuth.Basic(draft.username, draft.password)
            val session = client.session(draft.sessionUrl, auth, draft.trustedOrigins)
            if (verifyService(
                    session,
                    auth,
                )
            ) {
                save(draft, session)
            } else {
                ConnectOutcome.Failed(ConnectFailure.MissingService)
            }
        } catch (error: JmapException.RedirectedElsewhere) {
            ConnectOutcome.ConfirmHost(error.host, error.origin)
        } catch (error: JmapException) {
            ConnectOutcome.Failed(error.toFailure())
        } catch (expected: LoginStoreException) {
            // Storage did not confirm the save; the user can retry.
            ConnectOutcome.Failed(ConnectFailure.StorageFailed)
        }
    }

    override suspend fun remove(id: String) {
        storage { logins.remove(id) }
        connections.forget(id)
        onAccountChanged(id)
    }

    private suspend fun <T> storage(read: () -> T): T =
        withContext(Dispatchers.IO) {
            try {
                read()
            } catch (error: LoginStoreException) {
                throw AccountStorageException(error)
            }
        }

    /** A readable session can still deny the service; confirm its API answers before saving. */
    private suspend fun verifyService(
        session: JmapSession,
        auth: JmapAuth,
    ): Boolean {
        val accountId = session.accountFor(service.capability) ?: return false
        val probe = JmapType(service.probeType, service.capability, JsonObject.serializer())
        client.request(session, auth) { call(GetMethod(probe, accountId, properties = listOf("id"))) }
        return true
    }

    private suspend fun save(
        draft: JmapAccountDraft,
        session: JmapSession,
    ): ConnectOutcome {
        val login =
            withContext(Dispatchers.IO) {
                // Reconnecting the same user on the same server replaces its login instead of adding one.
                val existing =
                    logins.logins().firstOrNull {
                        it.username == draft.username &&
                            it.sessionUrl == draft.sessionUrl
                    }
                JmapLogin(
                    id = existing?.id ?: "${draft.username} ${draft.sessionUrl}",
                    sessionUrl = draft.sessionUrl,
                    username = draft.username,
                    name =
                        session.accountFor(service.capability)?.let { session.accounts[it]?.name }?.ifBlank { null }
                            ?: draft.username,
                    trustedOrigins = draft.trustedOrigins,
                ).also { logins.save(it, draft.password) }
            }
        connections.forget(login.id)
        onAccountChanged(login.id)
        return ConnectOutcome.Connected(login.toConnected())
    }
}

internal fun JmapException.toFailure(): ConnectFailure =
    when (this) {
        is JmapException.Unauthorized -> ConnectFailure.WrongCredentials
        is JmapException.Forbidden -> ConnectFailure.NotPermitted
        is JmapException.NotJmap, is JmapException.TooManyRedirects -> ConnectFailure.NotJmap
        is JmapException.Unsupported -> ConnectFailure.MissingService
        is JmapException.Unreachable -> ConnectFailure.Unreachable
        is JmapException.InsecureUrl -> ConnectFailure.Insecure
        else -> ConnectFailure.ServerError
    }

private fun JmapLogin.toConnected() =
    ConnectedAccount(
        id,
        name,
        username,
        runCatching {
            Url(sessionUrl).host
        }.getOrDefault(sessionUrl),
    )
