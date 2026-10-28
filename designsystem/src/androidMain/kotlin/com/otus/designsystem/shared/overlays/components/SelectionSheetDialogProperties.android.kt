package com.otus.designsystem.shared.overlays.components

import androidx.compose.ui.window.DialogProperties

internal actual fun selectionSheetDialogProperties(): DialogProperties =
    DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
