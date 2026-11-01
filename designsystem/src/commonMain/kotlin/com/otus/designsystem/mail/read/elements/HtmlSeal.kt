package com.otus.designsystem.mail.read.elements

/**
 * [html] that markup can safely follow. A tag cut off at the end is dropped, as a browser drops
 * it, and every element left open that would take in what comes after is closed: a comment or a
 * raw text element such as `<style>` would swallow it, an `<a>` would make it a link, and a
 * `<template>`, `<select>`, `<svg>`, or `<math>` would hide or misread it.
 */
internal fun sealedHtml(html: String): String {
    val scan = scanHtml(html)
    val open = OpenContainers()
    scan.tags.forEach { tag ->
        // A foreign element closes itself with `/>`; HTML elements ignore it.
        val opens = tag.name in CONTAINERS && !(tag.name in FOREIGN && tag.selfClosing)
        when {
            tag.closing -> open.close(tag.name)
            opens -> open.open(tag.name)
        }
    }
    return html.substring(0, scan.keep) + scan.tail + open.endTags()
}

/**
 * The containers left open, innermost last, and where each name last opened, so that closing one
 * never searches the list and the seal stays linear however deep the nesting.
 */
private class OpenContainers {
    private val names = mutableListOf<String>()
    private val positions = mutableMapOf<String, ArrayDeque<Int>>()

    fun open(name: String) {
        positions.getOrPut(name) { ArrayDeque() }.addLast(names.size)
        names += name
    }

    /** Closes the last open [name] and every element opened inside it. */
    fun close(name: String) {
        val position = positions[name]?.lastOrNull() ?: return
        while (names.size > position) positions.getValue(names.removeAt(names.lastIndex)).removeLast()
    }

    /** The end tags that close every open container, innermost first. */
    fun endTags(): String = names.asReversed().joinToString("") { "</$it>" }
}

/** Elements that would turn what follows into a link, hide it, or read it as something else. */
private val CONTAINERS =
    setOf("a", "button", "select", "template", "table", "svg", "math", "noscript", "object")

/** The only containers a `/>` closes. */
private val FOREIGN = setOf("svg", "math")
