package com.otus.designsystem.theme.tokens.overlays

import androidx.compose.ui.unit.dp

internal object ScopeSheetTokens {
    /** A top-level choice, such as every account, one account, or a domain. */
    val rowHeight = SheetTokens.rowHeight
    val avatar = 40.dp

    /** A choice nested under a group, such as one address of an account. */
    val childHeight = 52.dp
    val childAvatar = 32.dp

    /** Lines a nested choice's avatar up with its group's title. */
    val childIndent = avatar + SheetTokens.gap
}
