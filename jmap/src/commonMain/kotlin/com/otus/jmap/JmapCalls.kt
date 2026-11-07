package com.otus.jmap

import com.otus.jmap.login.LoginStoreException

/**
 * [call]'s result, or [fallback] when the server fails or the saved logins cannot be read. For
 * optional work whose failure the user need not see, such as a count or a suggestion.
 */
public inline fun <T> jmapCallOr(
    fallback: T,
    call: () -> T,
): T =
    try {
        call()
    } catch (expected: JmapException) {
        fallback
    } catch (expected: LoginStoreException) {
        fallback
    }
