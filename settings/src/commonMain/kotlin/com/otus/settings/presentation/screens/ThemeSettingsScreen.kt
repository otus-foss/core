package com.otus.settings.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.otus.designsystem.components.PageSection
import com.otus.designsystem.components.Subpage
import com.otus.designsystem.elements.ActionButton
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PanelTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.ThemeChoice
import com.otus.designsystem.theme.ThemeTokens
import com.otus.settings.presentation.ThemeSettingsState
import com.otus.settings.presentation.components.AccentPicker
import com.otus.settings.presentation.components.CustomAccentSheet
import com.otus.settings.presentation.components.ThemeCards

/**
 * Picks the theme and accent the whole app uses; a choice applies at once. [preview] shows a
 * sketch of the app's main screen, such as a mail list, in the current colors.
 */
@Composable
public fun ThemeSettingsScreen(
    state: ThemeSettingsState,
    onBack: () -> Unit,
    preview: @Composable () -> Unit,
) {
    val choice = state.choice
    var customizing by rememberSaveable { mutableStateOf(false) }
    Subpage("Theme", onBack) {
        val shape = RoundedCornerShape(SheetTokens.radius)
        Box(
            Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Preview of the app in this theme" }
                .background(OtusColors.bgRegular, shape)
                .border(PanelTokens.borderWidth, OtusColors.strokeBgDark, shape)
                .padding(ThemeTokens.previewPadding),
        ) { preview() }
        PageSection("Theme") {
            ThemeCards(choice) { state.select(choice.copy(base = it)) }
        }
        PageSection("Accent color") {
            AccentPicker(
                choice,
                onSelect = { state.select(choice.copy(accent = it)) },
                onCustom = { customizing = true },
            )
        }
        ActionButton("Reset to Otus", { state.select(ThemeChoice()) })
    }
    if (customizing) {
        CustomAccentSheet(
            choice.accent,
            onPick = { state.select(state.choice.copy(accent = it)) },
            onDismiss = { customizing = false },
        )
    }
}
