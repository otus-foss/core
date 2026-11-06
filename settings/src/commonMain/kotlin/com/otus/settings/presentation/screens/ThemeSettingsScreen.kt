package com.otus.settings.presentation.screens

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.otus.designsystem.shared.controls.elements.ActionButton
import com.otus.designsystem.shared.pages.components.PageBackdrop
import com.otus.designsystem.shared.pages.components.PageSection
import com.otus.designsystem.shared.pages.components.Subpage
import com.otus.designsystem.shared.settings.components.ColorSheet
import com.otus.designsystem.shared.settings.components.ThemePhonePanel
import com.otus.designsystem.shared.settings.components.ThemePhonePreview
import com.otus.designsystem.shared.settings.components.rememberPhoneExpansion
import com.otus.designsystem.theme.colors.OtusPalette
import com.otus.designsystem.theme.colors.ThemeBase
import com.otus.designsystem.theme.colors.ThemeChoice
import com.otus.designsystem.theme.colors.ThemeColor
import com.otus.designsystem.theme.colors.get
import com.otus.settings.presentation.ThemeSettingsState
import com.otus.settings.presentation.components.AccentPicker
import com.otus.settings.presentation.components.CustomAccentSheet
import com.otus.settings.presentation.components.ThemeCards
import com.otus.settings.presentation.components.ThemeColorRows
import com.otus.settings.presentation.components.ThemeDialog
import com.otus.settings.presentation.components.ThemeDialogs
import com.otus.settings.presentation.components.themeActions
import com.otus.settings.presentation.title

/**
 * Picks the theme and accent the whole app uses. Changing a single color makes a custom theme,
 * which stays until the user deletes it; a theme's actions duplicate, rename, or delete it. A
 * choice applies at once. A phone at the top shows the app in [preview], the current colors
 * unless [controls], shown below it, are trying others; a tap or drag down grows it and turns the
 * page's panel into the phone's.
 */
@Composable
public fun ThemeSettingsScreen(
    state: ThemeSettingsState,
    onBack: () -> Unit,
    preview: OtusPalette = state.choice.palette,
    controls: @Composable ColumnScope.() -> Unit = {},
) {
    val choice = state.choice
    var customizing by rememberSaveable { mutableStateOf(false) }
    var colorsShown by rememberSaveable { mutableStateOf(choice.customTheme != null) }
    var editing by rememberSaveable { mutableStateOf<ThemeColor?>(null) }
    var dialog by remember { mutableStateOf<ThemeDialog?>(null) }
    val phone = rememberPhoneExpansion()
    Subpage(
        "Theme",
        onBack,
        backdrop =
            PageBackdrop(
                content = { ThemePhonePreview(preview, phone) },
                panelOverlay = { ThemePhonePanel(preview, phone, Modifier.matchParentSize()) },
            ),
    ) {
        controls()
        PageSection("Theme") {
            ThemeCards(choice, state::select) { themeActions(it, choice, state::select) { next -> dialog = next } }
        }
        PageSection("Accent color") {
            AccentPicker(
                choice.accent,
                onSelect = { state.select(choice.withAccent(it)) },
                onCustom = { customizing = true },
            )
        }
        PageSection("Colors") {
            ThemeColorRows(choice, colorsShown, { colorsShown = !colorsShown }, { editing = it })
        }
        ActionButton(
            "Reset to Otus",
            { state.select(choice.withBase(ThemeBase.Otus).withAccent(ThemeChoice().accent)) },
        )
    }
    val currentDialog = dialog
    if (currentDialog != null) ThemeDialogs(currentDialog, choice, state::select) { dialog = it }
    if (customizing) {
        CustomAccentSheet(
            choice,
            onPick = { state.select(state.choice.withAccent(it)) },
            onDismiss = { customizing = false },
        )
    }
    val color = editing ?: return
    ColorSheet(
        color.title,
        choice.palette[color],
        onPick = { state.select(state.choice.withColor(color, it)) },
        onDismiss = { editing = null },
        onReset = { state.select(state.choice.withColor(color, value = null)) }.takeIf { color in choice.customColors },
    )
}
