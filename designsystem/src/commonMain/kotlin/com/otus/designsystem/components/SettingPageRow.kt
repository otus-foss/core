package com.otus.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_chevron_right
import org.jetbrains.compose.resources.DrawableResource

/**
 * A [SettingRow] that opens a page with [onOpen] and ends in a chevron. Its [description] holds
 * the page's current value or what the page holds, since a chevron row shows no value button.
 */
@Composable
public fun SettingPageRow(
    title: String,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: DrawableResource? = null,
) {
    SettingRow(
        title,
        onOpen,
        modifier,
        description,
        icon,
    ) { Symbol(Res.drawable.symbol_chevron_right, mirrorInRtl = true) }
}
