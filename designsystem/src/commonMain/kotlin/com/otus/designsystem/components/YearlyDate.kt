package com.otus.designsystem.components

/** A day of the year, and the [year] when it is known, such as a birthday without a year. */
public data class YearlyDate(
    val month: Int,
    val day: Int,
    val year: Int? = null,
)
