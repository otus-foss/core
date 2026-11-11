package com.otusfoss.core.designsystem.theme.tokens.content

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.otusfoss.core.designsystem.theme.tokens.layout.CornerTokens

/**
 * A file drawn as a page with a folded corner. The fills name kinds of files and stay the same in
 * every theme, since the page is a picture of the file and not a control. Each is dark enough for
 * the white type label on it.
 */
public object FileKindTokens {
    public val pdf: Color = Color(0xFFB0443A)
    public val document: Color = Color(0xFF3F6EA8)
    public val sheet: Color = Color(0xFF2F7D54)
    public val slides: Color = Color(0xFFA85F2A)
    public val archive: Color = Color(0xFF8A6234)
    public val media: Color = Color(0xFF5B55A8)
    public val calendar: Color = Color(0xFFA04A6A)

    /** A file whose kind has no color of its own. */
    public val other: Color = Color(0xFF5C6066)

    /** The type written on the page; the folded corner is a share of the same white. */
    public val label: Color = Color(0xFFFFFFFF)
    public const val FOLD_ALPHA: Float = 0.35f

    /** The page's width, corner, and fold as shares of its height or width. */
    public const val PAGE_RATIO: Float = 0.78f
    public const val CORNER: Float = 0.11f
    public const val FOLD: Float = 0.33f

    /** The label's size and its distance from the page's bottom, as shares of the icon's size. */
    public const val LABEL_SIZE: Float = 0.21f
    public const val LABEL_INSET: Float = 0.14f

    /** The corner of an image shown in place of the page. */
    public val pictureRadius: Dp = CornerTokens.small
}
