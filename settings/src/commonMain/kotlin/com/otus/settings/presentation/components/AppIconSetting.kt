package com.otus.settings.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.otus.designsystem.mail.brand.elements.MailAppIcon
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_owl
import com.otus.designsystem.shared.overlays.components.SheetFrame
import com.otus.designsystem.shared.selection.components.IconChoice
import com.otus.designsystem.shared.settings.components.SettingRow
import com.otus.designsystem.shared.settings.elements.ValueButton
import com.otus.designsystem.theme.brand.AppIconStyle
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.settings.presentation.AppIconSettingsState

/**
 * The App icon row and its sheet, a grid of the icons as the launcher shows them, for an app that
 * can switch its launcher icon. It goes in the appearance page's Look section.
 */
@Composable
public fun AppIconSetting(appIcon: AppIconSettingsState) {
    var choosing by rememberSaveable { mutableStateOf(false) }
    SettingRow(
        "App icon",
        { choosing = true },
        description = "Also colors the launch animation",
        icon = Res.drawable.symbol_owl,
    ) { ValueButton(appIcon.style.label) }
    if (choosing) {
        SheetFrame("App icon", onDismiss = { choosing = false }) {
            CardGrid(
                AppIconStyle.entries,
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(start = SheetTokens.inset, end = SheetTokens.inset, bottom = SheetTokens.inset)
                    .selectableGroup(),
                columns = ICON_COLUMNS,
            ) { style, modifier ->
                IconChoice(style.label, style == appIcon.style, { appIcon.select(style) }, modifier) {
                    MailAppIcon(style)
                }
            }
        }
    }
}

private const val ICON_COLUMNS = 3

private val AppIconStyle.label: String
    get() =
        when (this) {
            AppIconStyle.Accent -> "Accent"
            AppIconStyle.Monochrome -> "Monochrome"
            AppIconStyle.Chrome -> "Chrome"
            AppIconStyle.Rainbow -> "Rainbow"
            AppIconStyle.Trans -> "Trans"
            AppIconStyle.Bi -> "Bi"
            AppIconStyle.Lesbian -> "Lesbian"
        }
