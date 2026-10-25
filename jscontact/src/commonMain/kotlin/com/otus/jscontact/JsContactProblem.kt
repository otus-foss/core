package com.otus.jscontact

/**
 * A rule of RFC 9553 or RFC 9610 that a Card breaks, as [Card.validate] reports it. [path] is the JSON
 * Pointer (RFC 6901) of the value at fault within the Card, such as `/emails/e1/pref`, and [message] names
 * the rule and the section that defines it.
 */
public data class JsContactProblem(
    val path: String,
    val message: String,
)
