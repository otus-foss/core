package com.otus.designsystem.shared.overlays.components

import androidx.compose.ui.window.DialogProperties

internal actual fun sheetDialogProperties(): DialogProperties =
    DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
