package com.otus.designsystem.shared.controls.elements

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

/** A button that runs [onClick], announced with [label], or no button at all when [onClick] is null. */
internal fun Modifier.optionalButton(
    onClick: (() -> Unit)?,
    label: String? = null,
): Modifier = onClick?.let { clickable(onClickLabel = label, role = Role.Button, onClick = it) } ?: this
