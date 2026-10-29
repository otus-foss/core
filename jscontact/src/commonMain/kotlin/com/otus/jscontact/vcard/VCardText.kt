package com.otus.jscontact.vcard

import com.otus.jscontact.vcard.VCardKey.QUOTED_PRINTABLE

/** The line break vCard requires between content lines (RFC 6350, section 3.2). */
internal const val CRLF = "\r\n"

/**
 * The logical lines of vCard text. Line breaks may be CRLF, LF, or CR. A break followed by a space or a tab
 * folds a line and is removed (RFC 6350, section 3.2). A quoted-printable line that ends in `=` continues
 * on the next line, as vCard 2.1 writes soft line breaks. Blank lines are dropped.
 */
internal fun String.vCardLines(): List<String> {
    val physical =
        removePrefix(BYTE_ORDER_MARK)
            .replace(LINE_BREAK, "\n")
            .replace(FOLD, "")
            .split('\n')
    return buildList {
        for (line in physical) {
            val previous = lastOrNull()
            if (previous != null && previous.continuesQuotedPrintable()) {
                set(lastIndex, previous.dropLast(1) + line)
            } else if (line.isNotBlank()) {
                add(line)
            }
        }
    }
}

private fun String.continuesQuotedPrintable(): Boolean =
    endsWith('=') && substringBefore(':').contains(QUOTED_PRINTABLE, ignoreCase = true)

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
internal fun String.unescapeText(): String {
    val text = this
    return buildString {
        var escaped = false
        for (char in text) {
            when {
                escaped -> {
                    append(if (char == 'n' || char == 'N') '\n' else char)
                    escaped = false
                }

                char == '\\' -> {
                    escaped = true
                }

                else -> {
                    append(char)
                }
            }
        }
        if (escaped) append('\\')
    }
}

/** This text escaped for a vCard value: backslashes, line breaks, commas, and semicolons (RFC 6350, section 3.4). */
internal fun String.escapeText(): String =
    replace("\r\n", "\n")
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
            when {
                escaped -> {
                    part.append(char)
                    escaped = false
                }

                char == '\\' -> {
                    part.append(char)
                    escaped = true
                }

                char == delimiter -> {
                    add(part.toString())
                    part.clear()
                }

                else -> {
                    part.append(char)
                }
            }
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
        .replace("\n", "^n")
        .replace("\"", "^'")

private const val BYTE_ORDER_MARK = "\uFEFF"
private const val LINE_OCTETS = 75
private const val QUOTED_BYTE_LENGTH = 3
private const val HEX = 16
private const val LATIN_1 = "ISO-8859-1"

private val LINE_BREAK = Regex("\r\n?")
private val FOLD = Regex("\n[ \t]")
private val QUOTED_BYTES = Regex("(?:=[0-9A-Fa-f]{2})+")
private val CARET = Regex("""\^([n'^])""")
