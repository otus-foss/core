package com.otus.designsystem.components

/** Display text for a content row, prepared by its caller. */
public data class ContentRowText(
    public val title: String,
    public val subtitle: String,
    public val trailingText: String,
    public val metadata: String,
)
