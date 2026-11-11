package com.otusfoss.core.designsystem.mail.folders.components

import com.otusfoss.core.designsystem.shared.people.components.ChipAvatar
import org.jetbrains.compose.resources.DrawableResource

/** How a [RuleSegment] reads. */
public enum class RuleSegmentStyle {
    /** How the rule matches, such as "starts with", "over", or "isn't", in `text3`. */
    Dim,

    /** What the rule matches, such as an address or a quoted subject, in `text1`. */
    Bright,

    /** A wildcard, such as the `*` in a pattern, in `accentLight`. */
    Accent,
}

/** One run of a rule's value, drawn as written; include the spaces between runs in [text]. */
public data class RuleSegment(
    val text: String,
    val style: RuleSegmentStyle = RuleSegmentStyle.Bright,
)

/** The value of one rule condition, such as dim "starts with " then bright "\"[Acme]\"". */
public data class RuleChipValue(
    val segments: List<RuleSegment>,
) {
    /** The whole value as plain text, as screen readers hear it. */
    val text: String get() = segments.joinToString("") { it.text }
}

/**
 * One condition of a rule: its [label], such as "From" or "Subject", and its [value]. The slot
 * between them shows [avatar] for people and domains, or else [icon] in grey. A whole domain uses
 * the domain's letter on grey, and an own address uses that address's avatar; the caller builds
 * the [ChipAvatar].
 */
public data class RuleChipLine(
    val label: String,
    val value: RuleChipValue,
    val avatar: ChipAvatar? = null,
    val icon: DrawableResource? = null,
)
