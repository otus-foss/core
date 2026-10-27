package com.otus.designsystem.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.otus.designsystem.elements.FolderGlyphKind
import com.otus.designsystem.theme.FolderColor
import org.jetbrains.compose.resources.DrawableResource

/**
 * Display text for a [CompactContentRow], prepared by its caller. The date and the count pill
 * never shrink, so the line text truncates first.
 */
public data class CompactRowText(
    /** Line 1, 16sp SemiBold text1, one line, truncates first. */
    public val first: RowLine,
    /** Line 2, 14sp text2, one line. */
    public val second: RowLine,
    /** The trailing date, 13sp text2, never cut. */
    public val trailing: String,
    /** Count pill text, or null for none. Never cut. */
    public val count: String? = null,
    public val countPlacement: CountPlacement = CountPlacement.AfterFirst,
    /** Marks at the end of line 2, in this order, each 16dp with 4dp gaps. */
    public val marks: List<RowMark> = emptyList(),
    /** Folder glyphs after the marks; the caller limits them (max 2). */
    public val folders: List<RowFolderIcon> = emptyList(),
    /** "+N" after the folders in text3 11sp when > 0. */
    public val moreFolders: Int = 0,
    /** Optional preview under line 2: 14sp text3, ellipsis, [previewLines] 0, 1 or 2. */
    public val preview: String = "",
    public val previewLines: Int = 0,
    /** What screen readers say for the row, built from what it shows. */
    public val description: String = "",
    /** The row's state for screen readers, such as "Unread". */
    public val stateLabel: String = "",
)

/** Where a [CompactRowText.count] pill sits. */
public enum class CountPlacement { AfterFirst, AfterSecond, BeforeTrailing }

/** One line of a [CompactContentRow]. [icon], when set, comes first, such as a reply arrow. */
public data class RowLine(
    public val parts: List<RowTextPart>,
    public val icon: DrawableResource? = null,
)

/** [quiet] parts use text3 instead of the line's color. */
public data class RowTextPart(
    public val text: String,
    public val quiet: Boolean = false,
)

/**
 * A state icon at the end of a [CompactContentRow]'s second line. [Invite] marks a calendar
 * invitation apart from [Attachment], so an invite does not look like any other file.
 */
public enum class RowMark { Answered, Attachment, Invite, Flagged, ReadLater }

/** A folder an item is in; see [com.otus.designsystem.elements.FolderGlyph]. */
public data class RowFolderIcon(
    public val kind: FolderGlyphKind,
    public val color: FolderColor?,
)

/** How a [CompactContentRow] line draws its text. */
internal class LineStyle(
    val color: Color,
    val size: TextUnit,
    val lineHeight: TextUnit,
    val weight: FontWeight = FontWeight.Normal,
)
