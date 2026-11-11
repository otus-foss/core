package com.otusfoss.core.designsystem.shared.rows.components

/**
 * How much room a [SettingRow][com.otusfoss.core.designsystem.shared.settings.components.SettingRow] takes.
 * [Regular] rows are settings rows with hairlines between them. [Compact] rows are
 * [com.otusfoss.core.designsystem.theme.tokens.core.Sizes.compactRowHeight] tall without hairlines,
 * for folder lists, where more rows on screen matter more than separation.
 */
public enum class RowDensity { Regular, Compact }
