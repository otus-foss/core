package com.otus.designsystem.elements

/**
 * [html] that markup can safely follow. A tag cut off at the end is dropped, as a browser drops
 * it, and every element left open that would take in what comes after is closed: a comment or a
 * raw text element such as `<style>` would swallow it, an `<a>` would make it a link, and a
 * `<template>`, `<select>`, `<svg>`, or `<math>` would hide or misread it.
 */
internal fun sealedHtml(html: String): String = HtmlSealer(html).sealed()

/** Scans a document once, the way a browser tokenizes it, to learn what is still open at its end. */
private class HtmlSealer(
    private val html: String,
) {
    private val open = mutableListOf<String>()
    private var keep = html.length
    private var tail = ""

    fun sealed(): String {
        var index: Int? = 0
        while (index != null) index = next(index)
        return html.substring(0, keep) + tail + open.asReversed().joinToString("") { "</$it>" }
    }

    /** Scans the markup from [from], and returns where to go on, or null at the end. */
    private fun next(from: Int): Int? {
        val start = html.indexOf('<', from)
        val kind = html.getOrNull(start + 1)
        return when {
            start < 0 -> null
            html.startsWith(COMMENT_START, start) -> comment(start)
            kind == '!' || kind == '?' -> bogusComment(start)
            kind == '/' || kind?.isLetter() == true -> tag(start)
            else -> start + 1
        }
    }

    /** A comment ends at the first `-->`, which may share its dashes with the start, as in `<!-->`. */
    private fun comment(start: Int): Int? {
        val end = html.indexOf(COMMENT_END, start + 2)
        if (end < 0) tail = COMMENT_END
        return if (end < 0) null else end + COMMENT_END.length
    }

    /** A doctype or other `<!` and `<?` markup ends at the first `>`. */
    private fun bogusComment(start: Int): Int? {
        val end = html.indexOf('>', start)
        return if (end < 0) cut(start) else end + 1
    }

    private fun tag(start: Int): Int? {
        val end = tagEnd(start) ?: return cut(start)
        val closing = html[start + 1] == '/'
        val name =
            html
                .substring(start + if (closing) 2 else 1, end)
                .takeWhile { it.isLetterOrDigit() }
                .lowercase()
        return when {
            closing -> {
                close(name)
                end
            }

            name in RAW_TEXT -> {
                rawTextEnd(name, end)
            }

            else -> {
                val selfClosed = name in FOREIGN && html[end - 2] == '/'
                if (name in CONTAINERS && !selfClosed) open += name
                end
            }
        }
    }

    /** Where a tag that starts at [start] ends, past its `>`; a `>` inside a quoted value does not end it. */
    private fun tagEnd(start: Int): Int? {
        var index = start + 1
        var afterEquals = false
        while (index < html.length) {
            val char = html[index]
            if (char == '>') return index + 1
            if (afterEquals && (char == '"' || char == '\'')) {
                index = html.indexOf(char, index + 1).takeIf { it >= 0 } ?: html.length
            }
            afterEquals = char == '=' || (afterEquals && char.isWhitespace())
            index++
        }
        return null
    }

    /** Raw text such as a style sheet runs to its end tag; without one, the seal closes it. */
    private fun rawTextEnd(
        name: String,
        from: Int,
    ): Int? {
        val end = Regex("</$name(?=[\\s/>])", RegexOption.IGNORE_CASE).find(html, from)
        if (end == null) tail = "</$name>"
        return end?.range?.first
    }

    /** An end tag closes the last open element of its name and every element opened inside it. */
    private fun close(name: String) {
        val index = open.lastIndexOf(name)
        if (index >= 0) open.subList(index, open.size).clear()
    }

    /** Drops the cut-off markup from [start] on, and ends the scan. */
    private fun cut(start: Int): Int? {
        keep = start
        return null
    }
}

private const val COMMENT_START = "<!--"
private const val COMMENT_END = "-->"

/** Elements whose content is text up to their own end tag, so they would swallow what follows. */
private val RAW_TEXT = setOf("style", "script", "textarea", "title", "xmp", "iframe", "noembed", "noframes")

/** Elements that would turn what follows into a link, hide it, or read it as something else. */
private val CONTAINERS =
    setOf("a", "button", "select", "template", "table", "svg", "math", "noscript", "object")

/** The only containers a `/>` closes; HTML elements ignore it. */
private val FOREIGN = setOf("svg", "math")
