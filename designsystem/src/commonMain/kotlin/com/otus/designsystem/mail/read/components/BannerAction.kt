package com.otus.designsystem.mail.read.components

/** A button of an [ImageBanner]: its short [label] and what it does. */
public data class BannerAction(
    val label: String,
    val onClick: () -> Unit,
)
