package com.otus.designsystem.shared.controls.elements

import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.otus.designsystem.theme.PanelTokens
import org.jetbrains.compose.resources.DrawableResource

/**
 * Colors the leading icon of the rows inside it, such as a folder's
 * [com.otus.designsystem.theme.FolderColor.tint]. Null keeps each row's own icon color. Text,
 * backgrounds, the selected state, trailing icons, and disabled icons keep their colors.
 * [SidebarEntry][com.otus.designsystem.shared.sidebar.elements.SidebarEntry],
 * [ActionRow][com.otus.designsystem.shared.rows.elements.ActionRow], `SettingRow`, `SettingToggleRow`,
 * `ValuePicker`, and `ValueToken`
 * read it; a `ChoiceRow` takes its own `iconTint`.
 */
public val LocalIconTint: ProvidableCompositionLocal<Color?> = compositionLocalOf { null }

/** A row's leading [Symbol], in [LocalIconTint] while the row is [enabled]. */
@Composable
internal fun LeadingSymbol(
    resource: DrawableResource,
    size: Dp = PanelTokens.iconSize,
    enabled: Boolean = true,
) {
    val tint = LocalIconTint.current.takeIf { enabled } ?: LocalContentColor.current
    CompositionLocalProvider(LocalContentColor provides tint) { Symbol(resource, size = size) }
}
