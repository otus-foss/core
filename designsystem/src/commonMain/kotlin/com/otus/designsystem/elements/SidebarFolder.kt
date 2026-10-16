package com.otus.designsystem.elements

import org.jetbrains.compose.resources.DrawableResource

/**
 * How one [SidebarFolderEntry] or folder settings row reads: its [label], nesting [depth], an
 * optional [count], and an [icon] in place of the closed folder, such as for a shared folder.
 */
public data class SidebarFolder(
    val label: String,
    val depth: Int = 0,
    val count: String? = null,
    val icon: DrawableResource? = null,
)
