package com.otus.jscontact

/**
 * Collects [JsContactProblem]s while validation walks a Card. A scope stands for one JSON value at the
 * JSON Pointer [path], and [member], [entries], and [items] open scopes for the values inside it, so every
 * check reports the exact path of what it rejects.
 */
internal class ValidationScope(
    private val path: String,
    private val problems: MutableList<JsContactProblem>,
) {
    /** Reports [message] for this value, or for its member [token] when given. */
    fun report(
        message: String,
        token: String? = null,
    ) {
        problems += JsContactProblem(token?.let(::pointer) ?: path, message)
    }

    /** Reports [message] for this value, or for its member [token], unless [valid]. */
    fun ensure(
        valid: Boolean,
        message: String,
        token: String? = null,
    ) {
        if (!valid) report(message, token)
    }

    /** Runs [check] on [value], the member [token] of this value, when it is set. */
    fun <T : Any> member(
        token: String,
        value: T?,
        check: ValidationScope.(T) -> Unit,
    ) {
        if (value != null) ValidationScope(pointer(token), problems).check(value)
    }

    /** Runs [check] on each value of the map at member [token], in the scope of its key. */
    fun <T : Any> entries(
        token: String,
        values: Map<String, T>?,
        check: ValidationScope.(T) -> Unit,
    ) {
        member(token, values) { map -> map.forEach { (key, value) -> member(key, value, check) } }
    }

    /** Runs [check] on each item of the array at member [token], in the scope of its index. */
    fun <T : Any> items(
        token: String,
        values: List<T>?,
        check: ValidationScope.(T) -> Unit,
    ) {
        member(token, values) { list -> list.forEachIndexed { index, value -> member(index.toString(), value, check) } }
    }

    // A reference token escapes "~" as "~0" and "/" as "~1" (RFC 6901, section 3).
    private fun pointer(token: String): String = "$path/" + token.replace("~", "~0").replace("/", "~1")
}

/** Runs [check] in the scope of the root value and returns every problem it reported. */
internal fun validation(check: ValidationScope.() -> Unit): List<JsContactProblem> =
    buildList { ValidationScope("", this).check() }
