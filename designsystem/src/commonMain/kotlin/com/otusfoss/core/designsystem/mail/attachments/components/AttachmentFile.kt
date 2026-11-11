package com.otusfoss.core.designsystem.mail.attachments.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_attach_file
import com.otusfoss.core.designsystem.resources.symbol_code
import com.otusfoss.core.designsystem.resources.symbol_event
import com.otusfoss.core.designsystem.resources.symbol_folder_zip
import com.otusfoss.core.designsystem.resources.symbol_grid_on
import com.otusfoss.core.designsystem.resources.symbol_landscape
import com.otusfoss.core.designsystem.resources.symbol_movie
import com.otusfoss.core.designsystem.resources.symbol_music_note
import com.otusfoss.core.designsystem.resources.symbol_notes
import com.otusfoss.core.designsystem.resources.symbol_pdf
import com.otusfoss.core.designsystem.resources.symbol_person
import com.otusfoss.core.designsystem.resources.symbol_slideshow
import com.otusfoss.core.designsystem.theme.tokens.content.FileKindTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * What kind of file an [AttachmentFile] is, which picks the symbol on its round tile when it has
 * no thumbnail. Each symbol stands alone, without a frame of its own, so the tile is its frame.
 */
public enum class AttachmentKind(
    internal val icon: DrawableResource,
) {
    Image(Res.drawable.symbol_landscape),
    Pdf(Res.drawable.symbol_pdf),
    Document(Res.drawable.symbol_notes),
    Sheet(Res.drawable.symbol_grid_on),
    Slides(Res.drawable.symbol_slideshow),
    Archive(Res.drawable.symbol_folder_zip),
    Audio(Res.drawable.symbol_music_note),
    Video(Res.drawable.symbol_movie),
    Calendar(Res.drawable.symbol_event),
    Contact(Res.drawable.symbol_person),
    Code(Res.drawable.symbol_code),
    Other(Res.drawable.symbol_attach_file),
}

/**
 * The fill of the page a file of this kind is drawn as, when it has no picture. Kinds that share a
 * fill are still told apart by the type written on the page.
 */
internal val AttachmentKind.tint: Color
    get() =
        when (this) {
            AttachmentKind.Pdf -> FileKindTokens.pdf
            AttachmentKind.Document -> FileKindTokens.document
            AttachmentKind.Sheet -> FileKindTokens.sheet
            AttachmentKind.Slides -> FileKindTokens.slides
            AttachmentKind.Archive -> FileKindTokens.archive
            AttachmentKind.Image, AttachmentKind.Audio, AttachmentKind.Video -> FileKindTokens.media
            AttachmentKind.Calendar -> FileKindTokens.calendar
            AttachmentKind.Contact, AttachmentKind.Code, AttachmentKind.Other -> FileKindTokens.other
        }

/**
 * A file attached to a message, as an [AttachmentStrip] shows it: its [name], a [detail] such as
 * "PDF · 240 KB · 4 pages", its [kind], an optional decoded [thumbnail] for images, and the short
 * [label] of its type, such as "PDF", written on its page when it has no thumbnail.
 */
public data class AttachmentFile(
    val name: String,
    val detail: String,
    val kind: AttachmentKind,
    val thumbnail: ImageBitmap? = null,
    val label: String = "",
)

/**
 * What the files of an [AttachmentStrip] do: tapping a row runs [onOpen] and its download button
 * [onSave], both with the file's index; "Save all" runs [onSaveAll].
 */
public class AttachmentActions(
    public val onOpen: (Int) -> Unit,
    public val onSave: (Int) -> Unit,
    public val onSaveAll: () -> Unit,
)
