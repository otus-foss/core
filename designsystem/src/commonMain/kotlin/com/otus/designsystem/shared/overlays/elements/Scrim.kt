package com.otus.designsystem.shared.overlays.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.overlays.SheetTokens

/**
 * The dimmed backdrop of a sheet, dialog, or anything else that unfolds over a page, at [visible]
 * strength from 0 to 1. Tapping it runs [onClick], which screen readers announce as [label].
 */
@Composable
public fun Scrim(
    label: String,
    visible: Float,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(OtusColors.scrim.copy(alpha = SheetTokens.SCRIM_ALPHA * visible))
            .semantics { contentDescription = label }
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onClick),
    )
}
