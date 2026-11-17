package com.otusfoss.core.designsystem.mail.read.html

/**
 * [html] with its frames, plugins, and forms made inert. A frame or plugin opens a document of its
 * own, and a `srcdoc` frame fills it from markup that reaches this page only as escaped text, so
 * no pass over this page sees its tags. A form sends what the user typed to an address of the
 * sender's choosing.
 *
 * Their start and end tags get an inert name, as `<link>` does in [inertLinks], and a frame's
 * fallback text goes, since a browser never shows it and renaming the frame would parse it as
 * markup. Form controls keep their element, so a button drawn for looks still renders, but they
 * are disabled, so they take no text and submit nothing. Only real tags count, found in one linear
 * scan, so text, comments, and style sheets stay as they are.
 */
internal fun inertFramesAndForms(html: String): String {
    val scan = scanHtml(html)
    val edits =
        (
            scan.tags.mapNotNull(::inertEdit) +
                scan.rawTexts
                    .filter { it.name == "iframe" }
                    .map { HtmlEdit(it.start, it.end, "") }
        ).sortedBy { it.start }
    return html.withEdits(edits)
}

/** The edit that makes [tag] inert: a new name for a frame, plugin, or form, or `disabled` on a control. */
private fun inertEdit(tag: HtmlTag): HtmlEdit? {
    val nameStart = tag.start + if (tag.closing) 2 else 1
    val nameEnd = nameStart + tag.name.length
    return when {
        tag.name in RENAMED -> HtmlEdit(nameStart, nameEnd, "$INERT_PREFIX${tag.name}")
        tag.name in CONTROLS && !tag.closing -> HtmlEdit(nameEnd, nameEnd, " disabled")
        else -> null
    }
}

private const val INERT_PREFIX = "otus-inert-"

/** Elements that open a document of their own, or send what the user typed. */
private val RENAMED = setOf("iframe", "frame", "frameset", "object", "embed", "form")

/** Form controls, which stay for their looks. */
private val CONTROLS = setOf("input", "button", "select", "textarea")
