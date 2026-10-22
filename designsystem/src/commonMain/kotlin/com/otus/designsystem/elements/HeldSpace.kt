package com.otus.designsystem.elements

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout

/**
 * While [hidden], the content still takes its space but neither draws nor takes touches, so what is
 * laid out around it, such as a page's text, does not move when it hides and shows again.
 */
public fun Modifier.holdsSpace(hidden: Boolean): Modifier =
    if (!hidden) {
        this
    } else {
        layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {}
        }
    }
