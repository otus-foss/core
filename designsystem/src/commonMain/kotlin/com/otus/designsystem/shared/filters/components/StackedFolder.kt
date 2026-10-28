package com.otus.designsystem.shared.filters.components

import com.otus.designsystem.theme.colors.FolderColor
import org.jetbrains.compose.resources.DrawableResource

/** One folder of a [FolderStackChip]: its [color] and the [icon] it shows without one. */
public data class StackedFolder(
    val color: FolderColor?,
    val icon: DrawableResource,
)
