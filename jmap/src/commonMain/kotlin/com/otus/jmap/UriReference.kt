package com.otus.jmap

/** RFC 3986 section 5.2. Resolve components before Ktor parses the resulting absolute URL. */
internal fun resolveUriReference(
    base: String,
    reference: String,
): String {
    val source = UriReference.parse(base)
    val target = UriReference.parse(reference.trim())
    return when {
        target.scheme != null -> target.copy(path = removeDotSegments(target.path))
        target.authority != null -> target.copy(scheme = source.scheme, path = removeDotSegments(target.path))
        target.path.isEmpty() -> target.under(source, source.path, target.query ?: source.query)
        else -> target.under(source, removeDotSegments(source.merge(target.path)), target.query)
    }.render()
}

private data class UriReference(
    val scheme: String?,
    val authority: String?,
    val path: String,
    val query: String?,
    val fragment: String?,
) {
    /** This reference with [base]'s scheme and authority (section 5.2.2). */
    fun under(
        base: UriReference,
        path: String,
        query: String?,
    ): UriReference = copy(scheme = base.scheme, authority = base.authority, path = path, query = query)

    /** The [reference] path merged onto this base's path (section 5.2.3), before removing dot segments. */
    fun merge(reference: String): String = if (reference.startsWith('/')) reference else directory() + reference

    private fun directory(): String =
        if (authority != null && path.isEmpty()) "/" else path.substringBeforeLast('/', "") + "/"

    fun render(): String =
        buildString {
            if (scheme != null) append(scheme).append(':')
            if (authority != null) append("//").append(authority)
            append(path)
            if (query != null) append('?').append(query)
            if (fragment != null) append('#').append(fragment)
        }

    companion object {
        // Keep absent and empty query/fragment components distinct.
        private val syntax =
            Regex(
                "^(?:(?<scheme>[A-Za-z][A-Za-z0-9+.-]*):)?(?://(?<authority>[^/?#]*))?" +
                    "(?<path>[^?#]*)(?:\\?(?<query>[^#]*))?(?:#(?<fragment>.*))?$",
            )

        fun parse(text: String): UriReference {
            val match = syntax.matchEntire(text) ?: throw JmapException.InsecureUrl("invalid URI reference")
            return UriReference(
                match.groups["scheme"]?.value,
                match.groups["authority"]?.value,
                match.groups["path"]?.value.orEmpty(),
                match.groups["query"]?.value,
                match.groups["fragment"]?.value,
            )
        }
    }
}

/** Remove literal dot segments, preserving escaped dots and repeated slashes. */
private fun removeDotSegments(path: String): String {
    val segments = mutableListOf<String>()
    for (segment in path.split('/')) {
        when (segment) {
            "." -> Unit
            ".." -> if (segments.isNotEmpty() && segments != listOf("")) segments.removeAt(segments.lastIndex)
            else -> segments.add(segment)
        }
    }
    if (path.endsWith("/.") || path.endsWith("/..")) segments.add("")
    return segments.joinToString("/")
}
