package com.otusfoss.core.designsystem.mail.read.html

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.otusfoss.core.designsystem.theme.colors.toHex

/** A CSS rule whose [declarations] win over the message's own style sheets. */
internal fun cssRule(
    selector: String,
    vararg declarations: String,
): String = declarations.joinToString(";", "$selector{", "}") { "$it!important" }

/** A size in CSS pixels, which match density-independent pixels at the view's default zoom. */
internal fun Dp.css(): String = value.cssNumber() + PX

internal fun TextUnit.css(): String = value.cssNumber() + PX

internal fun Color.css(): String = toHex().lowercase()

private fun Float.cssNumber(): String = if (this % 1f == 0f) toInt().toString() else toString()

internal const val PX = "px"
