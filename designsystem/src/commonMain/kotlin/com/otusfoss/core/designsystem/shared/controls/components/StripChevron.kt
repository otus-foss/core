package com.otusfoss.core.designsystem.shared.controls.components

import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.otusfoss.core.designsystem.resources.Res
import com.otusfoss.core.designsystem.resources.symbol_expand_less
import com.otusfoss.core.designsystem.resources.symbol_expand_more
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.theme.colors.OtusColors
import com.otusfoss.core.designsystem.theme.tokens.rows.StripTokens

/** The `text2` chevron at a strip's end: up while it can grow, down while it can fold. */
@Composable
internal fun StripChevron(up: Boolean) {
    CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
        Symbol(
            if (up) Res.drawable.symbol_expand_less else Res.drawable.symbol_expand_more,
            size = StripTokens.chevronSize,
        )
    }
}
