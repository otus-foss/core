package com.otusfoss.core.designsystem.mail.sidebar.components

import com.otusfoss.core.designsystem.mail.sidebar.elements.SidebarTileSize

/**
 * One item of a [SidebarTileEditor]: a unique, stable [key], the [label] accessibility services
 * read, and the [size] it spans.
 */
public data class EditableTile(
    val key: Any,
    val label: String,
    val size: SidebarTileSize,
)
