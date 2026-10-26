package com.otus.designsystem.components

/**
 * The background a setting or section can explain on request: a [title] and the [paragraphs] an
 * [InfoTip] shows, such as who sees what when alerts arrive. It keeps such copy off the page.
 */
public data class InfoText(
    val title: String,
    val paragraphs: List<String>,
) {
    public constructor(title: String, text: String) : this(title, listOf(text))
}
