package com.otusfoss.core.accounts.domain

/**
 * The JMAP service an app needs from an account. Connecting checks that the session offers
 * [capability] and that a `[probeType]/get` call succeeds before anything is saved.
 */
public data class AccountService(
    /** Lowercase noun used in messages, such as "mail". */
    val name: String,
    val capability: String,
    val probeType: String,
)
