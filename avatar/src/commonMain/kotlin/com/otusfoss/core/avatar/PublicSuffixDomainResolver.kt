package com.otusfoss.core.avatar

import com.otusfoss.core.avatar.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Loads an ASCII-normalized Public Suffix List once, by default the snapshot this module bundles.
 * No runtime network lookup is needed.
 */
public class PublicSuffixDomainResolver(
    private val loadRules: suspend () -> String = ::bundledPublicSuffixRules,
) : AvatarDomainResolver {
    private val lock = Mutex()
    private var rules: PublicSuffixRules? = null

    override suspend fun resolve(host: String): String? =
        lock
            .withLock {
                val cached = rules
                if (cached != null) {
                    cached
                } else {
                    val loaded = withContext(Dispatchers.Default) { PublicSuffixRules(loadRules()) }
                    rules = loaded
                    loaded
                }
            }.registrableDomain(host)
}

/** The Public Suffix List snapshot in this module's resources; see docs/avatar.md. */
internal suspend fun bundledPublicSuffixRules(): String = Res.readBytes("files/public_suffix_list.dat").decodeToString()

/** PSL longest-rule matching, including private suffixes, wildcards, and exception rules. */
internal class PublicSuffixRules(
    text: String,
) {
    private val rules =
        text
            .lineSequence()
            .map(::ruleOf)
            .filter { it.isNotEmpty() && !it.startsWith("//") }
            .toList()
    private val exceptions = rules.filter { it.startsWith("!") }.map { it.drop(1) }.toSet()
    private val wildcards = rules.filter { it.startsWith("*.") }.map { it.drop(2) }.toSet()
    private val exact = rules.filterNot { it.startsWith("!") || it.startsWith("*.") }.toSet()

    fun registrableDomain(host: String): String? {
        if (!isPublicAvatarHost(host)) return null
        val labels = host.lowercase().split('.')
        // Each suffix of the host, longest first: "a.b.c", "b.c", "c".
        val suffixes = labels.indices.map { labels.drop(it).joinToString(".") }
        return suffixes.firstOrNull { it in exceptions } ?: domainAbovePublicSuffix(labels, suffixes)
    }

    /** The longest matching public suffix plus one more label, if the host has that label. */
    private fun domainAbovePublicSuffix(
        labels: List<String>,
        suffixes: List<String>,
    ): String? {
        val suffixLength =
            suffixes.indices
                .filter { suffixes[it] in exact || suffixes.getOrElse(it + 1) { "" } in wildcards }
                .maxOfOrNull { labels.size - it } ?: 1
        return labels.takeIf { it.size > suffixLength }?.takeLast(suffixLength + 1)?.joinToString(".")
    }
}

/** The rule on a list line: its first word, lowercased. Comment lines start with `//`. */
private fun ruleOf(line: String): String =
    line
        .trim()
        .substringBefore(' ')
        .substringBefore('\t')
        .lowercase()
