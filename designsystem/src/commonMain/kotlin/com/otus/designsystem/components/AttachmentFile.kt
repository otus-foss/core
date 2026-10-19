package com.otus.designsystem.components

import androidx.compose.ui.graphics.ImageBitmap
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_description
import com.otus.designsystem.resources.symbol_file_blank
import com.otus.designsystem.resources.symbol_file_pdf
import com.otus.designsystem.resources.symbol_file_sheet
import com.otus.designsystem.resources.symbol_image
import org.jetbrains.compose.resources.DrawableResource

/**
 * What kind of file an [AttachmentFile] is, which picks its symbol when it has no thumbnail. Every
 * kind but an image shows the same folded page, marked with lines, "PDF", or cells, or left blank.
 */
public enum class AttachmentKind(
    internal val icon: DrawableResource,
) {
    Image(Res.drawable.symbol_image),
    Pdf(Res.drawable.symbol_file_pdf),
    Sheet(Res.drawable.symbol_file_sheet),
    Document(Res.drawable.symbol_description),
    Other(Res.drawable.symbol_file_blank),
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
