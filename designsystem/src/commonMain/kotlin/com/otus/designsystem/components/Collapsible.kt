package com.otus.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.otus.designsystem.theme.motionTween

/**
 * [content] that unfolds downward and fades in while [expanded], and folds back up when it is
 * not, at the speed of the panel's chip rail and the [com.otus.designsystem.elements.SidebarExpandButton]
 * chevron. Use it for the rows nested under a collapsible row.
 */
@Composable
public fun Collapsible(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        expanded,
        modifier,
        enter = expandVertically(motionTween(), expandFrom = Alignment.Top) + fadeIn(motionTween()),
        exit = shrinkVertically(motionTween(), shrinkTowards = Alignment.Top) + fadeOut(motionTween()),
        label = "Collapsible rows",
    ) { content() }
}
