package com.otus.dateformat

/** Stable choices for numeric, Gregorian date display. Stored IDs must not use enum ordinals. */
public enum class DateFormatPreference(
    public val storageValue: String,
    public val patternLabel: String,
) {
    DeviceLocale("device", "Use device format"),
    DayMonthYearDots("dmy-dots", "DD.MM.YYYY"),
    DayMonthYearSlashes("dmy-slashes", "DD/MM/YYYY"),
    MonthDayYearDots("mdy-dots", "MM.DD.YYYY"),
    MonthDayYearSlashes("mdy-slashes", "MM/DD/YYYY"),
    YearMonthDayDashes("ymd-dashes", "YYYY-MM-DD"),
    YearMonthDaySlashes("ymd-slashes", "YYYY/MM/DD"),
    ;

    public companion object {
        public fun fromStorageValue(value: String?): DateFormatPreference =
            entries.firstOrNull { it.storageValue == value } ?: DeviceLocale
    }
}
