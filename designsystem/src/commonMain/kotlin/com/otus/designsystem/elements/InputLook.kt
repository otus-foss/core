package com.otus.designsystem.elements

import org.jetbrains.compose.resources.DrawableResource

/**
 * What an [InputBox] shows besides its value: the [placeholder], the keyboard [kind], and an [icon]
 * before the text.
 */
internal data class InputLook(
    val placeholder: String = "",
    val kind: InputKind = InputKind.Text,
    val icon: DrawableResource? = null,
)
