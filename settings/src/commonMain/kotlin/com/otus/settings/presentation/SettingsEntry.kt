package com.otus.settings.presentation

import org.jetbrains.compose.resources.DrawableResource

/** One row on the settings page, opening a subpage the app owns. */
public data class SettingsEntry(
    val title: String,
    val summary: String,
    val icon: DrawableResource?,
    val onOpen: () -> Unit,
)
