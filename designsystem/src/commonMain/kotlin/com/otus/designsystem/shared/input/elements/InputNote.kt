package com.otus.designsystem.shared.input.elements

/**
 * The line under an [InputField]: a hint about the value, or what is wrong with it. With [valid]
 * known, the field also marks its end with a check or a cross.
 */
public data class InputNote(
    val text: String,
    val valid: Boolean? = null,
)
