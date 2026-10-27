package com.otus.designsystem.shared.filters.components

import com.otus.designsystem.theme.FolderColor
import org.jetbrains.compose.resources.DrawableResource

/** One folder of a [FolderStackChip]: its [color] and the [icon] it shows without one. */
public data class StackedFolder(
    val color: FolderColor?,
    val icon: DrawableResource,
)
