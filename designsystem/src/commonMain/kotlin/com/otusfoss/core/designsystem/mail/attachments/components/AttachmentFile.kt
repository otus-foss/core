package com.otusfoss.core.designsystem.mail.attachments.components

import androidx.compose.ui.graphics.ImageBitmap
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_file_app
import com.otusfoss.core.designsystem.resources.symbol_file_app_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_archive
import com.otusfoss.core.designsystem.resources.symbol_file_archive_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_audio
import com.otusfoss.core.designsystem.resources.symbol_file_audio_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_calendar
import com.otusfoss.core.designsystem.resources.symbol_file_calendar_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_code
import com.otusfoss.core.designsystem.resources.symbol_file_code_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_contact
import com.otusfoss.core.designsystem.resources.symbol_file_contact_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_data
import com.otusfoss.core.designsystem.resources.symbol_file_data_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_database
import com.otusfoss.core.designsystem.resources.symbol_file_database_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_design
import com.otusfoss.core.designsystem.resources.symbol_file_design_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_disk_image
import com.otusfoss.core.designsystem.resources.symbol_file_disk_image_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_document
import com.otusfoss.core.designsystem.resources.symbol_file_document_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_ebook
import com.otusfoss.core.designsystem.resources.symbol_file_ebook_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_email
import com.otusfoss.core.designsystem.resources.symbol_file_email_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_encrypted
import com.otusfoss.core.designsystem.resources.symbol_file_encrypted_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_font
import com.otusfoss.core.designsystem.resources.symbol_file_font_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_image
import com.otusfoss.core.designsystem.resources.symbol_file_image_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_key
import com.otusfoss.core.designsystem.resources.symbol_file_key_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_location
import com.otusfoss.core.designsystem.resources.symbol_file_location_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_other
import com.otusfoss.core.designsystem.resources.symbol_file_other_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_pdf
import com.otusfoss.core.designsystem.resources.symbol_file_pdf_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_sheet
import com.otusfoss.core.designsystem.resources.symbol_file_sheet_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_signature
import com.otusfoss.core.designsystem.resources.symbol_file_signature_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_slides
import com.otusfoss.core.designsystem.resources.symbol_file_slides_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_text
import com.otusfoss.core.designsystem.resources.symbol_file_text_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_video
import com.otusfoss.core.designsystem.resources.symbol_file_video_detailed
import com.otusfoss.core.designsystem.resources.symbol_file_web
import com.otusfoss.core.designsystem.resources.symbol_file_web_detailed
import org.jetbrains.compose.resources.DrawableResource

/**
 * What kind of file an [AttachmentFile] is, which picks its symbol when it has no thumbnail: a page
 * with the type drawn on it, or a bare page for [Other]. The [icon] reads at 20 dp, on round tiles
 * and pills; the [detailedIcon] keeps the page's fold and draws the type with more parts, for files
 * shown 40 dp and up beside their name.
 */
public enum class AttachmentKind(
    internal val icon: DrawableResource,
    internal val detailedIcon: DrawableResource,
) {
    Image(Res.drawable.symbol_file_image, Res.drawable.symbol_file_image_detailed),
    Pdf(Res.drawable.symbol_file_pdf, Res.drawable.symbol_file_pdf_detailed),
    Document(Res.drawable.symbol_file_document, Res.drawable.symbol_file_document_detailed),
    Text(Res.drawable.symbol_file_text, Res.drawable.symbol_file_text_detailed),
    Sheet(Res.drawable.symbol_file_sheet, Res.drawable.symbol_file_sheet_detailed),
    Data(Res.drawable.symbol_file_data, Res.drawable.symbol_file_data_detailed),
    Slides(Res.drawable.symbol_file_slides, Res.drawable.symbol_file_slides_detailed),
    Design(Res.drawable.symbol_file_design, Res.drawable.symbol_file_design_detailed),
    Archive(Res.drawable.symbol_file_archive, Res.drawable.symbol_file_archive_detailed),
    DiskImage(Res.drawable.symbol_file_disk_image, Res.drawable.symbol_file_disk_image_detailed),
    Audio(Res.drawable.symbol_file_audio, Res.drawable.symbol_file_audio_detailed),
    Video(Res.drawable.symbol_file_video, Res.drawable.symbol_file_video_detailed),
    Calendar(Res.drawable.symbol_file_calendar, Res.drawable.symbol_file_calendar_detailed),
    Contact(Res.drawable.symbol_file_contact, Res.drawable.symbol_file_contact_detailed),
    Email(Res.drawable.symbol_file_email, Res.drawable.symbol_file_email_detailed),
    Web(Res.drawable.symbol_file_web, Res.drawable.symbol_file_web_detailed),
    Code(Res.drawable.symbol_file_code, Res.drawable.symbol_file_code_detailed),
    Key(Res.drawable.symbol_file_key, Res.drawable.symbol_file_key_detailed),
    Signature(Res.drawable.symbol_file_signature, Res.drawable.symbol_file_signature_detailed),
    Encrypted(Res.drawable.symbol_file_encrypted, Res.drawable.symbol_file_encrypted_detailed),
    App(Res.drawable.symbol_file_app, Res.drawable.symbol_file_app_detailed),
    Font(Res.drawable.symbol_file_font, Res.drawable.symbol_file_font_detailed),
    Ebook(Res.drawable.symbol_file_ebook, Res.drawable.symbol_file_ebook_detailed),
    Location(Res.drawable.symbol_file_location, Res.drawable.symbol_file_location_detailed),
    Database(Res.drawable.symbol_file_database, Res.drawable.symbol_file_database_detailed),
    Other(Res.drawable.symbol_file_other, Res.drawable.symbol_file_other_detailed),
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
