package com.otus.dateformat

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** Relative labels use the device's local calendar date, without changing stored dates. */
public fun DateFormatter.formatRelative(
    date: LocalDate,
    today: LocalDate = currentLocalDate(),
): String =
    when (date) {
        today -> "Today"
        today.minus(1, DateTimeUnit.DAY) -> "Yesterday"
        else -> format(date)
    }

public fun DateFormatter.formatRelativeRange(
    start: LocalDate,
    end: LocalDate,
    today: LocalDate = currentLocalDate(),
    separator: String = " → ",
): String = formatRelative(start, today) + separator + formatRelative(end, today)

public fun currentLocalDate(): LocalDate =
    Clock.System
        .now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
