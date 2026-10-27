package com.otus.designsystem.shared.dates.components

/** Display values and ordering supplied by the caller, independent of date-library types. */
public data class MonthNavigationLabel(
    val month: String,
    val year: String,
    val chronologicalOrder: Int,
)
