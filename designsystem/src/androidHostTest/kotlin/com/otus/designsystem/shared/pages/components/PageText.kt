package com.otus.designsystem.shared.pages.components

import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.otus.designsystem.screenshot.screenshot
import com.otus.designsystem.theme.OtusPalette
import com.otus.designsystem.theme.panelTextStyle

/** Gives [content] the panel text style that pages and sheets provide in the apps. */
@Composable
internal fun PageText(content: @Composable () -> Unit) = ProvideTextStyle(panelTextStyle(), content)

/** A [screenshot] of [content] in the [PageText] style, for components that take their text style from the page. */
internal fun ComposeContentTestRule.pageScreenshot(
    name: String,
    palette: OtusPalette = OtusPalette.Otus,
    content: @Composable () -> Unit,
) = screenshot(name, palette = palette) { PageText(content) }
