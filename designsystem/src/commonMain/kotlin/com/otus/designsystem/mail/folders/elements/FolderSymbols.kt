package com.otus.designsystem.mail.folders.elements

import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_archive
import com.otus.designsystem.resources.symbol_delete
import com.otus.designsystem.resources.symbol_draft
import com.otus.designsystem.resources.symbol_folder
import com.otus.designsystem.resources.symbol_inbox
import com.otus.designsystem.resources.symbol_report
import com.otus.designsystem.resources.symbol_schedule_send
import com.otus.designsystem.resources.symbol_send
import org.jetbrains.compose.resources.DrawableResource

/**
 * The symbol of a mailbox with the JMAP [role] (RFC 8621, section 2), such as `inbox` or `junk`.
 * The user's other folders, and roles without a symbol, share one; Read later uses
 * `symbol_bookmark`.
 */
public fun folderSymbol(role: String?): DrawableResource =
    when (role) {
        "inbox" -> Res.drawable.symbol_inbox
        "drafts" -> Res.drawable.symbol_draft
        "sent" -> Res.drawable.symbol_send
        "scheduled" -> Res.drawable.symbol_schedule_send
        "archive" -> Res.drawable.symbol_archive
        "junk" -> Res.drawable.symbol_report
        "trash" -> Res.drawable.symbol_delete
        else -> Res.drawable.symbol_folder
    }
