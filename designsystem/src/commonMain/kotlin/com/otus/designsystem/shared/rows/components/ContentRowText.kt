package com.otus.designsystem.shared.rows.components

import org.jetbrains.compose.resources.DrawableResource

/**
 * Display text for a content row, prepared by its caller. [labels] show as pills before
 * [metadata] on the last line.
 */
public data class ContentRowText(
    public val title: String,
    public val subtitle: String,
    public val trailingText: String,
    public val metadata: String,
    public val labels: List<RowLabel> = emptyList(),
)

/** A label pill on a content row; see [com.otus.designsystem.shared.controls.elements.LabelPill]. */
public data class RowLabel(
    public val text: String,
    public val icon: DrawableResource? = null,
    public val highlighted: Boolean = false,
)
