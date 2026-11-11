package com.otusfoss.core.jscontact

/** Whether [address] is an RFC 5322 addr-spec, such as "ada@example.org", as an EmailAddress needs. */
public fun isEmailAddress(address: String): Boolean = ADDR_SPEC.matches(address)

// Any code point from U+0080. A negated class also matches the code points above U+FFFF, such as
// emoji, which a range up to \uFFFF misses.
private const val NON_ASCII = """[^\x00-\x7F]"""

// atext (RFC 5322, section 3.2.3), plus non-ASCII as RFC 6532, section 3.2, allows in internationalized mail.
private const val ATEXT = """(?:[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]|$NON_ASCII)"""

private const val DOT_ATOM = """$ATEXT+(?:\.$ATEXT+)*"""

// quoted-string (RFC 5322, section 3.2.4): quotes around any text but '"', '\', CR, and LF, or a quoted pair.
private const val QUOTED_STRING = """"(?:[^"\\\r\n]|\\.)*""""

// domain-literal (RFC 5322, section 3.4.1): brackets around text without brackets, backslashes, or spaces.
private const val DOMAIN_LITERAL = """\[[^\[\]\\\s]*\]"""

// addr-spec = local-part "@" domain (RFC 5322, section 3.4.1), without the obsolete forms and comments.
private val ADDR_SPEC = Regex("(?:$DOT_ATOM|$QUOTED_STRING)@(?:$DOT_ATOM|$DOMAIN_LITERAL)")
