package com.otus.jmap

/** RFC 3986 section 5.2. Resolve components before Ktor parses the resulting absolute URL. */
internal fun resolveUriReference(
    base: String,
    reference: String,
): String {
    val source = UriReference.parse(base)
    val target = UriReference.parse(reference.trim())
    val resolved =
        when {
            target.scheme != null -> {
                target.copy(path = removeDotSegments(target.path))
            }

            target.authority != null -> {
                target.copy(scheme = source.scheme, path = removeDotSegments(target.path))
            }

            target.path.isEmpty() -> {
                target.copy(
                    scheme = source.scheme,
                    authority = source.authority,
                    path = source.path,
                    query = target.query ?: source.query,
                )
            }

            else -> {
                target.copy(
                    scheme = source.scheme,
                    authority = source.authority,
                    path =
                        removeDotSegments(
                            if (target.path.startsWith('/')) target.path else source.directory() + target.path,
                        ),
                )
            }
        }
    return resolved.render()
}

private data class UriReference(
    val scheme: String?,
    val authority: String?,
    val path: String,
    val query: String?,
    val fragment: String?,
) {
    fun directory(): String = if (authority != null && path.isEmpty()) "/" else path.substringBeforeLast('/', "") + "/"

    fun render(): String =
        buildString {
            scheme?.let { append(it).append(':') }
            authority?.let { append("//").append(it) }
            append(path)
            query?.let { append('?').append(it) }
            fragment?.let { append('#').append(it) }
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
