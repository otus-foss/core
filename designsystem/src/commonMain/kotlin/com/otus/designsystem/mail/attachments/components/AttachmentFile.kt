package com.otus.designsystem.mail.attachments.components

import androidx.compose.ui.graphics.ImageBitmap
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_attach_file
import com.otus.designsystem.resources.symbol_code
import com.otus.designsystem.resources.symbol_event
import com.otus.designsystem.resources.symbol_folder_zip
import com.otus.designsystem.resources.symbol_grid_on
import com.otus.designsystem.resources.symbol_landscape
import com.otus.designsystem.resources.symbol_movie
import com.otus.designsystem.resources.symbol_music_note
import com.otus.designsystem.resources.symbol_notes
import com.otus.designsystem.resources.symbol_pdf
import com.otus.designsystem.resources.symbol_person
import com.otus.designsystem.resources.symbol_slideshow
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
 * A file attached to a message, as an [AttachmentStrip] shows it: its [name], a [detail] such as
 * "PDF · 240 KB · 4 pages", its [kind], and an optional decoded [thumbnail] for images.
 */
public data class AttachmentFile(
    val name: String,
    val detail: String,
    val kind: AttachmentKind,
    val thumbnail: ImageBitmap? = null,
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
