package com.otus.accounts.testing

import com.otus.jmap.login.LoginLifecycle

/** A [LoginLifecycle] that runs the steps a test gives it. */
class TestLifecycle(
    private val onConnected: suspend (String) -> Unit = {},
    private val onRemoving: suspend (String) -> Unit = {},
    private val onRemoveFailed: suspend (String) -> Unit = {},
    private val onRemoved: suspend (String) -> Unit = {},
) : LoginLifecycle {
    override suspend fun connected(loginId: String) = onConnected(loginId)

    override suspend fun removing(loginId: String) = onRemoving(loginId)

    override suspend fun removeFailed(loginId: String) = onRemoveFailed(loginId)

    override suspend fun removed(loginId: String) = onRemoved(loginId)
}
