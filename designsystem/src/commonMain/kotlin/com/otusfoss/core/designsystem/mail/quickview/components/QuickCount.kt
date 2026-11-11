package com.otusfoss.core.designsystem.mail.quickview.components

import org.jetbrains.compose.resources.DrawableResource

/** One slice of mail in [QuickCounts]: its [label], [icon], how many [count] it holds, and [onClick]. */
public class QuickCount(
    public val label: String,
    public val icon: DrawableResource,
    public val count: Int,
    public val onClick: () -> Unit,
)
