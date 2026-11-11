package com.otusfoss.core.jscontact.vcard

import com.otusfoss.core.jscontact.vcard.VCardKey.QUOTED_PRINTABLE

/** The line break vCard requires between content lines (RFC 6350, section 3.2). */
internal const val CRLF = "\r\n"

/**
 * The logical lines of vCard text. Line breaks may be CRLF, LF, or CR. A quoted-printable line that ends in `=`
 * continues on the next line, as vCard 2.1 writes soft line breaks, and that line is kept whole, a leading space
 * included. Otherwise a line that starts with a space or a tab continues the previous one without that character
 * (RFC 6350, section 3.2). Blank lines are dropped.
 */
internal fun String.vCardLines(): List<String> {
    val joiner = LineJoiner()
    val physical = removePrefix(BYTE_ORDER_MARK).split(LINE_BREAK)
    return (physical.mapNotNull(joiner::read) + joiner.finish()).filter { it.isNotBlank() }
}

/** Joins physical lines into logical lines, copying each part once so that long values join in linear time. */
private class LineJoiner {
    private val line = StringBuilder()

    /** Whether the line's parameters name quoted-printable, or null until its colon has been read. */
    private var quoted: Boolean? = null

    /** Adds [physical] to the current line, and returns the finished line when [physical] starts a new one. */
    fun read(physical: String): String? {
        val softBreak = quoted == true && line.endsWith('=')
        val folded = !softBreak && (physical.startsWith(' ') || physical.startsWith('\t'))
        val finished = if (softBreak || folded) null else finish()
        if (softBreak) line.deleteAt(line.lastIndex)
        append(physical, from = if (folded) 1 else 0)
        return finished
    }

    /** The current line, after which the next part starts a new one. */
    fun finish(): String {
        val finished = line.toString()
        line.clear()
        quoted = null
        return finished
    }

    private fun append(
        part: String,
        from: Int,
    ) {
        line.appendRange(part, from, part.length)
        if (quoted == null && part.indexOf(':', from) >= 0) {
            quoted = line.substring(0, line.indexOf(':')).contains(QUOTED_PRINTABLE, ignoreCase = true)
        }
    }
}

/**
 * This content line folded so that no line is longer than 75 octets of UTF-8, not counting the line break
 * (RFC 6350, section 3.2). Each continuation starts with a space, and no character is split.
 */
internal fun String.folded(): String {
    val line = this
    return buildString {
        var width = 0
        var index = 0
        while (index < line.length) {
            val end = index + if (line[index].isHighSurrogate() && index + 1 < line.length) 2 else 1
            val size = line.substring(index, end).encodeToByteArray().size
            if (width + size > LINE_OCTETS) {
                append(CRLF).append(' ')
                width = 1
            }
            append(line.substring(index, end))
            width += size
            index = end
        }
    }
}

/** This value with its backslash escapes resolved: `\n` or `\N`, `\,`, `\;`, and `\\` (RFC 6350, section 3.4). */
internal fun String.unescapeText(): String =
    replace(TEXT_ESCAPE) { match ->
        val char = match.groupValues[1]
        if (char == "n" || char == "N") "\n" else char
    }

/**
 * This text escaped for a vCard value: backslashes, line breaks, commas, and semicolons (RFC 6350, section 3.4).
 * A lone CR is a line break too, since [vCardLines] reads it as one.
 */
internal fun String.escapeText(): String =
    replace("\r\n", "\n")
        .replace('\r', '\n')
        .replace("\\", "\\\\")
        .replace("\n", "\\n")
        .replace(",", "\\,")
        .replace(";", "\\;")

/**
 * This value split at each [delimiter] that no backslash escapes, as structured values such as N and ADR
 * and lists such as NICKNAME need (RFC 6350, section 3.4). Each part keeps its escapes.
 */
internal fun String.splitUnescaped(delimiter: Char): List<String> {
    val text = this
    return buildList {
        val part = StringBuilder()
        var escaped = false
        for (char in text) {
            if (char == delimiter && !escaped) {
                add(part.toString())
                part.clear()
            } else {
                part.append(char)
            }
            // A backslash escapes the next character, unless the backslash is itself escaped.
            escaped = !escaped && char == '\\'
        }
        add(part.toString())
    }
}

/**
 * The values of a list, such as NICKNAME or one component of ADR, split at unescaped commas, with their escapes
 * resolved and their outer whitespace trimmed. Empty values are dropped.
 */
internal fun String.textValues(): List<String> =
    splitUnescaped(',').map { it.unescapeText().trim() }.filter { it.isNotEmpty() }

/**
 * This quoted-printable value decoded (RFC 2045, section 6.7), as vCard 2.1 and 3.0 write it. The bytes are
 * UTF-8 unless [charset] is ISO-8859-1. Other charsets also decode as UTF-8, because common Kotlin has no
 * charset tables.
 */
internal fun String.decodeQuotedPrintable(charset: String?): String =
    replace(QUOTED_BYTES) { match ->
        val bytes =
            match.value
                .chunked(QUOTED_BYTE_LENGTH)
                .map { it.drop(1).toInt(HEX).toByte() }
                .toByteArray()
        if (charset.equals(LATIN_1, ignoreCase = true)) {
            bytes.map { Char(it.toUByte().toInt()) }.joinToString("")
        } else {
            bytes.decodeToString()
        }
    }

/** This parameter value with RFC 6868 caret escapes resolved: `^n` is a line break, `^'` a quote, `^^` a caret. */
internal fun String.decodeCarets(): String =
    replace(CARET) { match ->
        when (match.groupValues[1]) {
            "n" -> "\n"
            "'" -> "\""
            else -> "^"
        }
    }

/** This parameter value with carets, line breaks, and quotes escaped as RFC 6868 describes. */
internal fun String.encodeCarets(): String =
    replace("^", "^^")
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .replace("\n", "^n")
        .replace("\"", "^'")

private const val BYTE_ORDER_MARK = "\uFEFF"
private const val LINE_OCTETS = 75
private const val QUOTED_BYTE_LENGTH = 3
private const val HEX = 16
private const val LATIN_1 = "ISO-8859-1"

private val LINE_BREAK = Regex("\r\n?|\n")
private val QUOTED_BYTES = Regex("(?:=[0-9A-Fa-f]{2})+")
private val CARET = Regex("""\^([n'^])""")

// A backslash and the character it escapes, which may be any character, a line break included.
private val TEXT_ESCAPE = Regex("""\\([\s\S])""")
