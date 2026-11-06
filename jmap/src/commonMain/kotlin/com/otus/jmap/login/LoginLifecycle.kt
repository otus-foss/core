package com.otus.jmap.login

/**
 * What keeps state for a saved login, such as a cache or a push subscription, and must follow the
 * login's life. The account repository calls each step for every registered lifecycle, in order.
 * Every step does nothing unless overridden.
 */
public interface LoginLifecycle {
    /** The login was saved, new or with new credentials. */
    public suspend fun connected(loginId: String) {}

    /**
     * The login is about to go, and its credentials still exist, so server-side state can be
     * deleted. Throwing stops the removal, and then every lifecycle gets [removeFailed].
     */
    public suspend fun removing(loginId: String) {}

    /** The removal stopped before the credentials went, so the login is still saved. */
    public suspend fun removeFailed(loginId: String) {}

    /** The login and its credentials are gone. */
    public suspend fun removed(loginId: String) {}
}
