package com.otusfoss.core.designsystem.shared.controls.elements

/** How a two-tone [Symbol] draws its back part. */
public enum class SymbolTone {
    /** The back part at its lighter alpha, the default on plain backgrounds. */
    Duo,

    /**
     * Both parts opaque: the front in the content color and the back in that color mixed with the
     * page. For a symbol whose color carries meaning, such as a colored folder, so the color reads
     * at full strength and nothing behind it shows through.
     */
    Shaded,

    /** Every part opaque in the content color, keeping the gaps, for a symbol on another color. */
    Solid,
}
