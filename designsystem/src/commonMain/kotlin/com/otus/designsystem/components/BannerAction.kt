package com.otus.designsystem.components

/** A button of an [ImageBanner]: its short [label] and what it does. */
public data class BannerAction(
    val label: String,
    val onClick: () -> Unit,
)
