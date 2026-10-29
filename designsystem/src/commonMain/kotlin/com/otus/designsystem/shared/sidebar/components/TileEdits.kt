package com.otus.designsystem.shared.sidebar.components

import androidx.compose.ui.geometry.Offset
import com.otus.designsystem.shared.sidebar.elements.SidebarTileSize

/**
 * What one tile of a [SidebarTileEditor] can do. [sizeAt] gives the size nearest a width in
 * pixels, while its resize arc is dragged.
 */
internal class TileEdits(
    val label: String,
    val size: SidebarTileSize,
    val sizeAt: (Float) -> SidebarTileSize,
    val onResize: (SidebarTileSize) -> Unit,
    val onRemove: () -> Unit,
    val moves: TileMoves,
)

/**
 * How one tile of a [SidebarTileEditor] moves: a long press lifts it, then it is dragged and
 * dropped, or the drag is cancelled. Accessibility services move it one place [onEarlier] or
 * [onLater], which are null at either end.
 */
internal class TileMoves(
    val onLift: () -> Unit,
    val onDrag: (Offset) -> Unit,
    val onDrop: () -> Unit,
    val onCancel: () -> Unit,
    val onEarlier: (() -> Unit)?,
    val onLater: (() -> Unit)?,
)
