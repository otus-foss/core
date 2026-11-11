package com.otusfoss.core.designsystem.shared.pages.elements

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics

/** Leaves the accessibility tree while [hidden], such as a layer faded out or a page covered by another. */
public fun Modifier.hiddenWhen(hidden: Boolean): Modifier = if (hidden) clearAndSetSemantics {} else this
