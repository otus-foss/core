package com.otusfoss.core.designsystem.mail.read.html

/**
 * [html] with its frames, plugins, and forms made inert. A frame or plugin opens a document of its
 * own, and a `srcdoc` frame fills it from markup that reaches this page only as escaped text, so
 * no pass over this page sees its tags. A form sends what the user typed to an address of the
 * sender's choosing.
 *
 * Their start and end tags get an inert name by a plain text replace, as `<link>` does in
 * [inertLinks]. A browser sees one of these tags only where the markup spells its name after a
 * `<`, so the replace cannot miss one, whatever comes before it. [scanHtml] cannot promise that:
 * it reads some markup differently from a browser, such as a comment closed by `--!>`. The price
 * is that the same letters inside a comment or an attribute value change too, where they do
 * nothing. A renamed frame shows its fallback markup, which passes through every other check as
 * the rest of the message does.
 *
 * Form controls keep their element, so a button drawn for looks still renders, but they are
 * disabled, so they take no text. That part uses the scanner and is for looks only: without a
 * form, a control has nowhere to send anything.
 */
internal fun inertFramesAndForms(html: String): String =
    html
        .replace(DOCUMENT_OR_FORM_TAG) { "<${it.groupValues[1]}$INERT_PREFIX${it.groupValues[2].lowercase()}" }
        .let { renamed -> renamed.withEdits(scanHtml(renamed).tags.mapNotNull(::disabledEdit)) }

/** The edit that adds `disabled` to a form control's start [tag], or null for any other tag. */
private fun disabledEdit(tag: HtmlTag): HtmlEdit? =
    (tag.start + 1 + tag.name.length)
        .takeIf { tag.name in CONTROLS && !tag.closing }
        ?.let { nameEnd -> HtmlEdit(nameEnd, nameEnd, " disabled") }

private const val INERT_PREFIX = "otus-inert-"

/**
 * A start or end tag of an element that opens a document of its own or sends what the user typed.
 * The name ends at whitespace, `/`, or `>`, in any case, as in [inertLinks].
 */
private val DOCUMENT_OR_FORM_TAG =
    Regex("<(/?)(iframe|frame|frameset|object|embed|form)(?=[\\s/>])", RegexOption.IGNORE_CASE)

/** Form controls, which stay for their looks. */
private val CONTROLS = setOf("input", "button", "select", "textarea")
