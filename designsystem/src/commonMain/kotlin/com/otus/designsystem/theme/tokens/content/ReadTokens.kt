package com.otus.designsystem.theme.tokens.content

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otus.designsystem.theme.tokens.layout.CornerTokens
import com.otus.designsystem.theme.tokens.overlays.ActionBarTokens

/**
 * The read view: the strips above its action bar, the banner and quote fold in a message body,
 * and the sender details. Strips round like the action bar they sit on.
 */
public object ReadTokens {
    /** A strip card: its corner, its border, and its content inset from the start and end. */
    public val stripRadius: Dp = ActionBarTokens.radius
    public val stripStart: Dp = 12.dp
    public val stripEnd: Dp = 10.dp

    /** Between the parts of a strip row. */
    public val stripGap: Dp = 10.dp

    /** The thread position strip and the newsletter strip. */
    public val positionHeight: Dp = 60.dp

    /** The closed attachment strip, and the header of the opened one. */
    public val attachmentHeight: Dp = 52.dp

    /** The "Also in" strip, and its wider inset, since its text has no avatar or icon before it. */
    public val slimHeight: Dp = 48.dp
    public val slimStart: Dp = 24.dp
    public val slimEnd: Dp = 20.dp

    /** A strip's avatar, its title, and the small line under it. */
    public val stripAvatarSize: Dp = 32.dp
    public val titleSize: TextUnit = 15.sp
    public val detailSize: TextUnit = 12.sp
    public val chevronSize: Dp = 20.dp

    /** Between the dots and the position text under a strip's title. */
    public val dotsGap: Dp = 8.dp

    /** Position dots: a plain dot, the wide current one, and the gap between them. */
    public val dotSize: Dp = 6.dp
    public val currentDotWidth: Dp = 18.dp
    public val dotGap: Dp = 3.dp

    /** The dots at the edge of a cut window: next to the edge, and at it. */
    public val nearEdgeDot: Dp = 4.dp
    public val edgeDot: Dp = 3.dp

    /** The most dots shown at once; more slide a window that keeps the current one in view. */
    public const val DOT_WINDOW: Int = 7

    /** The grown thread list: its header, its rows, their avatar, and the row text. */
    public val listHeaderHeight: Dp = 56.dp
    public val listAvatarSize: Dp = 26.dp
    public val previewSize: TextUnit = 14.sp
    public val dateSize: TextUnit = 13.sp

    /** The tallest the grown thread list gets, header included, before its rows scroll. */
    public val threadListMaxHeight: Dp = 360.dp

    /** Folder glyphs stacked in a strip, and how far each overlaps the one before. */
    public val folderGlyphSize: Dp = 16.dp
    public val folderOverlap: Dp = 4.dp
    public const val FOLDER_STACK_LIMIT: Int = 3

    /** The folder names in a strip, and the gap between a glyph and its name. */
    public val hintSize: TextUnit = 15.sp
    public val glyphGap: Dp = 6.dp

    /** A newsletter strip's leading symbol. */
    public val infoIconSize: Dp = 24.dp

    /** The outlined button at the end of a strip, with its symbol and label. */
    public val stripButtonHeight: Dp = 40.dp
    public val stripButtonPadding: Dp = 12.dp
    public val buttonIconSize: Dp = 18.dp
    public val buttonTextSize: TextUnit = 14.sp

    /** The widest a strip button's label gets before it ends in an ellipsis. */
    public val stripButtonLabelMaxWidth: Dp = 140.dp

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

    /** A plain text action such as "Save all": its symbol and label. */
    public val plainIconSize: Dp = 18.dp
    public val plainTextSize: TextUnit = 14.sp
    public val plainPadding: Dp = 8.dp

    /** The remote images banner, its text, and its button. */
    public val bannerHeight: Dp = 48.dp
    public val bannerRadius: Dp = CornerTokens.row
    public val bannerStart: Dp = 16.dp
    public val bannerEnd: Dp = 6.dp
    public val bannerTextSize: TextUnit = 13.sp
    public val bannerButtonHeight: Dp = 36.dp

    /** The sender details table: the label column, its text, and the row padding. */
    public val detailsLabelWidth: Dp = 56.dp
    public val detailsLabelSize: TextUnit = 13.sp
    public val detailsPadding: Dp = 8.dp

    /** The quote fold: its start border, the inset after it, and its text. */
    public val quoteBorder: Dp = 2.dp
    public val quoteInset: Dp = 12.dp

    /** The chevron after a quote's date that shows the full date and time. */
    public val dateToggleSize: Dp = 16.dp
    public val quoteTextSize: TextUnit = 14.sp
    public val quoteLineHeight: TextUnit = 21.sp
    public const val QUOTE_FOLDED_LINES: Int = 3

    /** Where the fade over a folded quote starts, as a share of its height. */
    public const val QUOTE_FADE_START: Float = 0.3f

    /** The person pill in a quote header, its avatar, and the fold toggle's symbol. */
    public val quotePillHeight: Dp = 26.dp
    public val quoteAvatarSize: Dp = 20.dp
    public val quotePillStart: Dp = 3.dp
    public val quoteToggleIcon: Dp = 16.dp

    /** In an HTML body: the space above the quote fold, between its header and text, and the initials size. */
    public val quoteGap: Dp = 16.dp
    public val quoteHeaderGap: Dp = 8.dp
    public val quoteInitialsSize: TextUnit = 9.sp

    /** A blocked remote image without a size of its own, its dashed border, corner, and mark. */
    public val placeholderHeight: Dp = 120.dp
    public val placeholderBorder: Dp = 2.dp
    public val placeholderRadius: Dp = CornerTokens.chip
    public val placeholderIcon: Dp = 24.dp
}
