package com.otus.designsystem.elements

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Names that one rendered document uses and its own markup cannot guess: the link its image
 * placeholders open, which [HtmlView] accepts only exactly, and the prefix of its added classes,
 * so the message's style sheets cannot select them.
 */
internal class RenderKey(
    nonce: String,
) {
    val showImagesUrl: String = "otus-images:show?k=$nonce"
    private val prefix = "otus-$nonce-"

    fun className(name: String): String = prefix + name
}

@OptIn(ExperimentalUuidApi::class)
internal fun newRenderKey(): RenderKey = RenderKey(Uuid.random().toHexString())

/** Whether [url] is a placeholder's request to show the blocked images of the document [key] rendered. */
internal fun isShowImagesLink(
    url: String,
    key: RenderKey,
): Boolean = url == key.showImagesUrl
