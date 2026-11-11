package com.otusfoss.core.settings.presentation.screens

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.shared.controls.elements.ActionButton
import com.otusfoss.core.designsystem.shared.pages.components.PageBackdrop
import com.otusfoss.core.designsystem.shared.pages.components.PageSection
import com.otusfoss.core.designsystem.shared.pages.components.Subpage
import com.otusfoss.core.designsystem.shared.settings.components.ColorSheet
import com.otusfoss.core.designsystem.shared.settings.components.ThemePhonePanel
import com.otusfoss.core.designsystem.shared.settings.components.ThemePhonePreview
import com.otusfoss.core.designsystem.shared.settings.components.rememberPhoneExpansion
import com.otusfoss.core.designsystem.theme.colors.OtusPalette
import com.otusfoss.core.designsystem.theme.colors.ThemeBase
import com.otusfoss.core.designsystem.theme.colors.ThemeChoice
import com.otusfoss.core.designsystem.theme.colors.ThemeColor
import com.otusfoss.core.designsystem.theme.colors.get
import com.otusfoss.core.settings.presentation.ThemeSettingsState
import com.otusfoss.core.settings.presentation.components.ThemeCards
import com.otusfoss.core.settings.presentation.components.ThemeColorRows
import com.otusfoss.core.settings.presentation.components.ThemeDialog
import com.otusfoss.core.settings.presentation.components.ThemeDialogs
import com.otusfoss.core.settings.presentation.components.themeActions
import com.otusfoss.core.settings.presentation.title

/**
 * Picks the theme the whole app uses. Changing a single color makes a custom theme,
 * which stays until the user deletes it; a theme's actions duplicate, rename, or delete it. The
 * System theme hides the accent and colors, which come from the device. A
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
        // The device's colors stay as the device sets them.
        if (choice.activeSystem == null) {
            PageSection("Colors") {
                ThemeColorRows(choice, colorsShown, { colorsShown = !colorsShown }, { editing = it })
            }
        }
        ActionButton(
            "Reset to Otus",
            { state.select(choice.withBase(ThemeBase.Otus).withAccent(ThemeChoice().accent)) },
        )
    }
    val currentDialog = dialog
    if (currentDialog != null) ThemeDialogs(currentDialog, choice, state::select) { dialog = it }
    val color = editing ?: return
    ColorSheet(
        color.title,
        choice.palette[color],
        onPick = { state.select(state.choice.withColor(color, it)) },
        onDismiss = { editing = null },
        onReset = { state.select(state.choice.withColor(color, value = null)) }.takeIf { color in choice.customColors },
    )
}
