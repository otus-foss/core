package com.otus.designsystem.components

import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.otus.designsystem.elements.Symbol
import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_expand_less
import com.otus.designsystem.resources.symbol_expand_more
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.ReadTokens

/** The `text2` chevron at a strip's end: up while it can grow, down while it can fold. */
@Composable
internal fun StripChevron(up: Boolean) {
    CompositionLocalProvider(LocalContentColor provides OtusColors.text2) {
        Symbol(
            if (up) Res.drawable.symbol_expand_less else Res.drawable.symbol_expand_more,
            size = ReadTokens.chevronSize,
        )
    }
}
