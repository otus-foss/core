package com.otus.designsystem.mail.folders.elements

import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_archive
import com.otus.designsystem.resources.symbol_bookmark
import com.otus.designsystem.resources.symbol_delete
import com.otus.designsystem.resources.symbol_draft
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.resources.symbol_inbox
import com.otus.designsystem.resources.symbol_report
import com.otus.designsystem.resources.symbol_send
import org.jetbrains.compose.resources.DrawableResource

/**
 * The symbol of a mailbox with the JMAP [role] (RFC 8621, section 2), such as `inbox` or `junk`,
 * or of the Read later folder. The user's other folders, and roles without a symbol, share one.
 */
public fun folderSymbol(
    role: String?,
    readLater: Boolean = false,
): DrawableResource =
    when {
        readLater -> Res.drawable.symbol_bookmark
        role == "inbox" -> Res.drawable.symbol_inbox
        role == "drafts" -> Res.drawable.symbol_draft
        role == "sent" -> Res.drawable.symbol_send
        role == "archive" -> Res.drawable.symbol_archive
        role == "junk" -> Res.drawable.symbol_report
        role == "trash" -> Res.drawable.symbol_delete
        else -> Res.drawable.symbol_folder
    }
