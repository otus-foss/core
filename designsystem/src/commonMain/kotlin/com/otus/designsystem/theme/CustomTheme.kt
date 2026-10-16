package com.otus.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * A theme the user made by changing [colors] of a built-in [base] theme. It stays until the user
 * deletes it; [id] tells custom themes apart and numbers their names.
 */
@Immutable
public data class CustomTheme(
    public val id: Int,
    public val base: ThemeBase,
    public val colors: Map<ThemeColor, Color> = emptyMap(),
)
