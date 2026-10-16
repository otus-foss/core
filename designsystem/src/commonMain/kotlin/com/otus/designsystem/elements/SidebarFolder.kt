package com.otus.designsystem.elements

/** How one [SidebarFolderEntry] reads: its [label], nesting [depth], and an optional [count]. */
public data class SidebarFolder(
    val label: String,
    val depth: Int = 0,
    val count: String? = null,
)
