package com.otusfoss.core.settings.presentation.components

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_owl
import com.otusfoss.core.designsystem.shared.overlays.components.SheetFrame
import com.otusfoss.core.designsystem.shared.overlays.elements.sheetBodyInset
import com.otusfoss.core.designsystem.shared.selection.components.IconChoice
import com.otusfoss.core.designsystem.theme.brand.AppIconStyle
import com.otusfoss.core.settings.presentation.AppIconSettingsState

/**
 * The App icon row and its sheet, a grid of the icons as the launcher shows them, for an app that
 * can switch its launcher icon. [icon] draws the app's icon in each style. It goes in the
 * appearance page's Look section, among `appearanceSections`' rows.
 */
public fun appIconRow(
    appIcon: AppIconSettingsState,
    icon: @Composable (AppIconStyle) -> Unit,
): ChoiceRowSpec =
    ChoiceRowSpec(
        "App icon",
        appIcon.style.label,
        { AppIconSheet(appIcon, icon, it) },
        AppIconStyle.entries.map { it.label },
        Res.drawable.symbol_owl,
        description = "Also colors the launch animation",
        keywords = listOf("Launcher", "Home screen"),
    )

@Composable
private fun AppIconSheet(
    appIcon: AppIconSettingsState,
    icon: @Composable (AppIconStyle) -> Unit,
    onDismiss: () -> Unit,
) {
    SheetFrame("App icon", onDismiss = onDismiss) {
        CardGrid(
            AppIconStyle.entries,
            Modifier
                .verticalScroll(rememberScrollState())
                .sheetBodyInset()
                .selectableGroup(),
            columns = ICON_COLUMNS,
        ) { style, modifier ->
            IconChoice(style.label, style == appIcon.style, { appIcon.select(style) }, modifier) {
                icon(style)
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
