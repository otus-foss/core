package com.otus.designsystem.components

/**
 * One page of a [SlidingPositionStrip]: its [heading], its [detail], such as "5/6 · 10 Oct", and
 * what screen readers hear, [description], or the strip's own wording while it is null.
 */
public class PositionPage(
    public val heading: NamedAvatar,
    public val detail: String,
    public val description: String? = null,
)
