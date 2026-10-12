package com.otus.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.otus.designsystem.elements.IconButton
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_settings
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.SidebarTokens

/**
 * The [Sidebar]'s leading rail. Its [items], such as
 * [com.otus.designsystem.elements.SidebarRailAccount], scroll when they overflow; the Settings
 * button stays pinned below them.
 */
@Composable
public fun SidebarRail(
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    items: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .width(SidebarTokens.railWidth)
            .fillMaxHeight()
            .trailingStroke(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = SidebarTokens.inset),
            verticalArrangement = Arrangement.spacedBy(SidebarTokens.railGap),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = items,
        )
        IconButton("Settings", onSettings, Modifier.padding(bottom = SidebarTokens.inset), outlined = false) {
            CompositionLocalProvider(
                LocalContentColor provides OtusColors.text2,
            ) { Symbol(Res.drawable.symbol_settings) }
        }
    }
}
