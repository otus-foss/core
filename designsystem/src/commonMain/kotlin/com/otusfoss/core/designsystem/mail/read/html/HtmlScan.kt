package com.otusfoss.core.designsystem.mail.read.html

/**
 * The markup of [html], read once from start to end the way a browser tokenizes it, so the work
 * grows with the document's length whatever it holds. Comments and the text of raw text elements,
 * such as a style sheet, hold no tags.
 */
internal fun scanHtml(html: String): HtmlScan = HtmlScanner(html).scan()

/**
 * What [scanHtml] found: the [tags] in order and the [rawTexts] they enclose. Markup cut off at
 * the end starts at [keep], and [tail] closes a comment or raw text element left open there.
 */
internal class HtmlScan(
    val tags: List<HtmlTag>,
    val rawTexts: List<HtmlRawText>,
    val keep: Int,
    val tail: String,
) {
    /** The text of every style sheet, including one left open at the end. */
    val styleSheets: List<String> get() = rawTexts.filter { it.name == "style" }.map { it.text }
}

/** A start or end tag that runs from [start] to [end], past its `>`, with its lowercase [name]. */
internal class HtmlTag(
    private val html: String,
    val start: Int,
    val end: Int,
    val name: String,
    val closing: Boolean,
    private val nameEnd: Int,
) {
    val text: String get() = html.substring(start, end)

    /** Whether the tag ends in `/>`, which closes it only in SVG and MathML. */
    val selfClosing: Boolean get() = html[end - 2] == '/'

    /** A start tag's attributes by lowercase name; a repeated name keeps its first value, as in a browser. */
    val attributes: Map<String, String> by lazy {
        if (closing) emptyMap() else AttributeReader(html, nameEnd, end - 1).read()
    }
}

/** The text of a raw text element such as `<style>`, from [start] to [end]. */
internal class HtmlRawText(
    private val html: String,
    val name: String,
    val start: Int,
    val end: Int,
) {
    val text: String get() = html.substring(start, end)
}

private class HtmlScanner(
    private val html: String,
) {
    private val tags = mutableListOf<HtmlTag>()
    private val rawTexts = mutableListOf<HtmlRawText>()
    private var keep = html.length
    private var tail = ""

    fun scan(): HtmlScan {
        var index: Int? = 0
        while (index != null) index = next(index)
        return HtmlScan(tags, rawTexts, keep, tail)
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
        val nameStart = start + if (closing) 2 else 1
        var nameEnd = nameStart
        while (nameEnd < end && html[nameEnd].isLetterOrDigit()) nameEnd++
        val name = html.substring(nameStart, nameEnd).lowercase()
        tags += HtmlTag(html, start, end, name, closing, nameEnd)
        return if (!closing && name in RAW_TEXT) rawText(name, end) else end
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

    /** Raw text such as a style sheet runs to its end tag; without one, the [tail] closes it. */
    private fun rawText(
        name: String,
        from: Int,
    ): Int? {
        val end = endTagStart(name, from)
        rawTexts += HtmlRawText(html, name, from, end ?: html.length)
        if (end == null) tail = "</$name>"
        return end
    }

    /** Where the first end tag [name] at or after [from] starts, in any case, or null without one. */
    private fun endTagStart(
        name: String,
        from: Int,
    ): Int? {
        var index = html.indexOf("</", from)
        while (index >= 0) {
            val named = html.regionMatches(index + 2, name, 0, name.length, ignoreCase = true)
            if (named && endsTagName(html.getOrNull(index + 2 + name.length))) return index
            index = html.indexOf("</", index + 2)
        }
        return null
    }

    /** Drops the cut-off markup from [start] on, and ends the scan. */
    private fun cut(start: Int): Int? {
        keep = start
        return null
    }
}

/** Reads the attributes between a tag's name, at [index], and its `>`, at [end]. */
private class AttributeReader(
    private val html: String,
    private var index: Int,
    private val end: Int,
) {
    fun read(): Map<String, String> =
        buildMap {
            skip(::separatesAttributes)
            while (index < end) {
                val name = name()
                val value = value()
                if (name !in this) put(name, value)
                skip(::separatesAttributes)
            }
        }

    /** A name runs to whitespace, `/`, or `=`, though it may start with `=`. */
    private fun name(): String {
        val start = index++
        skip { !separatesAttributes(it) && it != '=' }
        return html.substring(start, index).lowercase()
    }

    /** The value after `=`, in quotes or up to whitespace; empty without `=`. */
    private fun value(): String {
        skip(Char::isWhitespace)
        if (index >= end || html[index] != '=') return ""
        index++
        skip(Char::isWhitespace)
        return if (index < end && (html[index] == '"' || html[index] == '\'')) quoted() else unquoted()
    }

    /** A value in quotes; one cut off by the tag's end runs to it. */
    private fun quoted(): String {
        val start = index + 1
        val stop = html.indexOf(html[index], start).takeIf { it in start until end } ?: end
        index = minOf(stop + 1, end)
        return html.substring(start, stop)
    }

    private fun unquoted(): String {
        val start = index
        skip { !it.isWhitespace() }
        return html.substring(start, index)
    }

    private fun skip(test: (Char) -> Boolean) {
        while (index < end && test(html[index])) index++
    }
}

/** Whether [char] separates attributes: whitespace, or a stray `/`. */
private fun separatesAttributes(char: Char): Boolean = char.isWhitespace() || char == '/'

/** Whether [char], after `</` and a raw text element's name, ends that name. */
private fun endsTagName(char: Char?): Boolean = char != null && (char.isWhitespace() || char == '/' || char == '>')

private const val COMMENT_START = "<!--"
private const val COMMENT_END = "-->"

/** Elements whose content is text up to their own end tag, so they would swallow what follows. */
private val RAW_TEXT = setOf("style", "script", "textarea", "title", "xmp", "iframe", "noembed", "noframes")
