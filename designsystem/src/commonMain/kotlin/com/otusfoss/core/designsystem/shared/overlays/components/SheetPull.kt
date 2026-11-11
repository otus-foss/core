package com.otusfoss.core.designsystem.shared.overlays.components

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * A finger that opens a sheet by dragging up from outside it, such as from a strip it belongs to.
 * The sheet rises with [dragBy] and, once [release]d, opens the rest of the way after an upward
 * flick or more than half shown, else slides back and dismisses. Provide it as [LocalSheetPull]
 * around the sheet, which reads it as it opens.
 */
@Stable
public class SheetPull {
    internal var pulled: Float by mutableFloatStateOf(0f)
        private set

    /** The velocity the finger was let go at, in pixels a second, down when positive; null while it drags. */
    internal var releasedAt: Float? by mutableStateOf(null)
        private set

    /** Follows a finger that moved [dy] pixels, up when negative. */
    public fun dragBy(dy: Float) {
        pulled = (pulled - dy).coerceAtLeast(0f)
    }

    /** Lets go at [velocity] pixels a second. */
    public fun release(velocity: Float) {
        releasedAt = velocity
    }
}

/** The [SheetPull] opening the sheet composed inside, or null for a sheet that slides up by itself. */
public val LocalSheetPull: ProvidableCompositionLocal<SheetPull?> = staticCompositionLocalOf { null }
