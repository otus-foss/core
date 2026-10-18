package com.otus.designsystem.components

import androidx.compose.ui.graphics.ImageBitmap
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_attach_file
import com.otus.designsystem.resources.symbol_description
import com.otus.designsystem.resources.symbol_image
import com.otus.designsystem.resources.symbol_picture_as_pdf
import com.otus.designsystem.resources.symbol_table
import org.jetbrains.compose.resources.DrawableResource

/** What kind of file an [AttachmentFile] is, which picks its symbol when it has no thumbnail. */
public enum class AttachmentKind(
    internal val icon: DrawableResource,
) {
    Image(Res.drawable.symbol_image),
    Pdf(Res.drawable.symbol_picture_as_pdf),
    Sheet(Res.drawable.symbol_table),
    Document(Res.drawable.symbol_description),
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
