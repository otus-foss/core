package com.otus.designsystem.shared.rows.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.otus.designsystem.theme.motion.motionTween

/**
 * [content] that unfolds downward and fades in while [expanded], and folds back up when it is
 * not, at the speed of the
 * [com.otus.designsystem.shared.sidebar.elements.SidebarExpandButton] chevron. Use it for the rows
 * nested under a collapsible row, or a part that comes and goes, such as the panel's chip rail.
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
