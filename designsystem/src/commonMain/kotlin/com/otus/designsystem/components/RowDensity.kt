package com.otus.designsystem.components

/**
 * How much room a [SettingRow] takes. [Regular] rows are settings rows with hairlines between
 * them. [Compact] rows are [com.otus.designsystem.theme.PanelTokens.compactRowHeight] tall
 * without hairlines, for folder lists, where more rows on screen matter more than separation.
 */
public enum class RowDensity { Regular, Compact }
