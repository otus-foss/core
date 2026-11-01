package com.otus.settings.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.shared.controls.elements.ActionButton
import com.otus.designsystem.shared.pages.components.PageSection
import com.otus.designsystem.shared.pages.components.Subpage
import com.otus.designsystem.shared.settings.components.ColorSheet
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.colors.ThemeBase
import com.otus.designsystem.theme.colors.ThemeChoice
import com.otus.designsystem.theme.colors.ThemeColor
import com.otus.designsystem.theme.colors.get
import com.otus.designsystem.theme.shapes.SmoothCornerShape
import com.otus.designsystem.theme.shapes.strokeBorder
import com.otus.designsystem.theme.tokens.layout.PanelTokens
import com.otus.designsystem.theme.tokens.overlays.SheetTokens
import com.otus.designsystem.theme.tokens.settings.ThemeTokens
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
 * choice applies at once. [preview] shows a sketch of the app's main screen, such as a mail list,
 * in the current colors.
 */
@Composable
public fun ThemeSettingsScreen(
    state: ThemeSettingsState,
    onBack: () -> Unit,
    preview: @Composable () -> Unit,
) {
    val choice = state.choice
    var customizing by rememberSaveable { mutableStateOf(false) }
    var colorsShown by rememberSaveable { mutableStateOf(choice.customTheme != null) }
    var editing by rememberSaveable { mutableStateOf<ThemeColor?>(null) }
    var dialog by remember { mutableStateOf<ThemeDialog?>(null) }
    Subpage("Theme", onBack) {
        AppPreview(preview)
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

@Composable
private fun AppPreview(preview: @Composable () -> Unit) {
    val shape = SmoothCornerShape(SheetTokens.radius)
    Box(
        Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Preview of the app in this theme" }
            .background(OtusColors.bgRegular, shape)
            .strokeBorder(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape)
            .padding(ThemeTokens.previewPadding),
    ) { preview() }
}
