package com.otusfoss.core.designsystem.shared.input.elements

import org.jetbrains.compose.resources.DrawableResource

/**
 * What an [InputBox] shows besides its value: the [placeholder], the keyboard [kind], an [icon]
 * before the text, and whether the value is [valid], marked at its end. Null marks nothing.
 */
internal data class InputLook(
    val placeholder: String = "",
    val kind: InputKind = InputKind.Text,
    val icon: DrawableResource? = null,
    val valid: Boolean? = null,
)
