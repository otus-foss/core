package com.otus.designsystem.theme.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * A theme the user made by changing [colors] of a built-in [base] theme. It stays until the user
 * deletes it. [id] tells custom themes apart; without a [name] from the user, the app names the
 * theme by its number.
 */
@Immutable
public data class CustomTheme(
    public val id: Int,
    public val base: ThemeBase,
    public val colors: Map<ThemeColor, Color> = emptyMap(),
    public val name: String? = null,
)
