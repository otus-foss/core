package com.otus.accounts.domain

/** A connected account as the app shows it. */
public data class ConnectedAccount(
    val id: String,
    val name: String,
    val username: String,
    val server: String,
)

public enum class ConnectFailure {
    WrongCredentials,
    NotPermitted,
    InvalidUsername,
    NotJmap,
    MissingService,
    Unreachable,
    Insecure,
    ServerError,
    StorageFailed,
}

public sealed interface ConnectOutcome {
    public data class Connected(
        val account: ConnectedAccount,
    ) : ConnectOutcome

    public data class Failed(
        val reason: ConnectFailure,
    ) : ConnectOutcome

    /** Sign-in would go to [host], which the user did not enter. Retry with [origin] trusted to continue. */
    public data class ConfirmHost(
        val host: String,
        val origin: String,
    ) : ConnectOutcome
}

public interface AccountRepository {
    public suspend fun accounts(): List<ConnectedAccount>

    /** Verifies the server and credentials before anything is saved. */
    public suspend fun connect(draft: JmapAccountDraft): ConnectOutcome

    public suspend fun remove(id: String)
}

/** A local account operation failed; callers may retry without treating it as a server rejection. */
public class AccountStorageException(
    cause: Throwable,
) : Exception("Account storage is unavailable.", cause)
