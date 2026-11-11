package com.otusfoss.core.designsystem.shared.pages.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otusfoss.core.designsystem.theme.tokens.layout.PageTokens

/**
 * Keeps a page clear of the navigation bar and keyboard. The top stays open: the control panel
 * draws behind the status bar and pads its own content.
 */
@Composable
public fun Modifier.belowControlPanel(): Modifier =
    windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))

/** A page body's inset from the screen's sides, clear of any display cutout there. */
@Composable
public fun Modifier.pageSideInset(): Modifier =
    windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
        .padding(horizontal = PageTokens.pageInset)
