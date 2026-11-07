package com.otus.designsystem.theme.tokens.content

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otus.designsystem.theme.tokens.core.Spacing
import com.otus.designsystem.theme.tokens.layout.CornerTokens
import com.otus.designsystem.theme.typography.TypeScale

/**
 * The read view: the strips above its action bar, the banner and quote fold in a message body,
 * and the sender details. Strips round like the action bar they sit on.
 */
public object ReadTokens {
    /** The closed attachment strip, and the header of the opened one. */
    public val attachmentHeight: Dp = 52.dp

    /** The "Also in" strip, and its wider inset, since its text has no avatar or icon before it. */
    public val slimHeight: Dp = 48.dp
    public val slimStart: Dp = 24.dp
    public val slimEnd: Dp = 20.dp

    /** A strip's avatar, its title, and the small line under it. */
    public val stripAvatarSize: Dp = 32.dp
    public val detailSize: TextUnit = TypeScale.detail

    /** Between the dots and the position text under a strip's title. */
    public val dotsGap: Dp = 8.dp

    /** The grown thread list: its header, its rows, their avatar, and the row text. */
    public val listHeaderHeight: Dp = 56.dp
    public val listAvatarSize: Dp = 26.dp
    public val previewSize: TextUnit = 14.sp
    public val dateSize: TextUnit = 13.sp

    /** The tallest the grown thread list gets, header included, before its rows scroll. */
    public val threadListMaxHeight: Dp = 360.dp

    /** The folder names in a strip, and the gap between a glyph and its name. */
    public val hintSize: TextUnit = 15.sp

    /** The closed attachment strip's overlapping thumbnails. */
    public val thumbSize: Dp = 28.dp
    public val thumbOverlap: Dp = 8.dp
    public val thumbRing: Dp = 2.dp
    public val thumbIconSize: Dp = 16.dp
    public const val THUMB_LIMIT: Int = 3

    /** The closed attachment summary. */
    public val summarySize: TextUnit = 14.sp

    /** A file row in the opened attachment strip, and its thumbnail tile. */
    public val fileRowHeight: Dp = 60.dp
    public val fileTileSize: Dp = 40.dp

    /** The tallest the opened attachment rows get before they scroll. */
    public val openStripMaxHeight: Dp = 320.dp

    /** The remote images banner, its text, and its button. */
    public val bannerHeight: Dp = 48.dp
    public val bannerRadius: Dp = CornerTokens.row
    public val bannerStart: Dp = 16.dp
    public val bannerEnd: Dp = 6.dp
    public val bannerTextSize: TextUnit = 13.sp
    public val bannerButtonHeight: Dp = 36.dp

    /** The quote fold: its start border, the inset after it, and its text. */
    public val quoteBorder: Dp = 2.dp
    public val quoteInset: Dp = 12.dp

    public val quoteTextSize: TextUnit = 14.sp
    public val quoteLineHeight: TextUnit = 21.sp
    public const val QUOTE_FOLDED_LINES: Int = 3

    /** Where the fade over a folded quote starts, as a share of its height. */
    public const val QUOTE_FADE_START: Float = 0.3f

    public val quoteToggleIcon: Dp = 16.dp

    /** In an HTML body: the space above the quote fold, between its header and text, and the initials size. */
    public val quoteGap: Dp = 16.dp
    public val quoteHeaderGap: Dp = 8.dp
    public val quoteInitialsSize: TextUnit = 9.sp

    /** Space between the parts of a Read page: the header, the body, and the files under it. */
    public val partGap: Dp = Spacing.l

    /** A blocked remote image without a size of its own, its dashed border, corner, and mark. */
    public val placeholderHeight: Dp = 120.dp
    public val placeholderBorder: Dp = 2.dp
    public val placeholderRadius: Dp = CornerTokens.chip
    public val placeholderIcon: Dp = 24.dp
}
