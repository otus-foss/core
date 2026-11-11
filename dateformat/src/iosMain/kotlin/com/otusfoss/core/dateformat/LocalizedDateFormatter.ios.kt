package com.otusfoss.core.dateformat

import kotlinx.datetime.LocalDate
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localeIdentifier
import platform.Foundation.timeZoneForSecondsFromGMT

public actual fun currentDateLocaleIdentifier(): String = NSLocale.currentLocale.localeIdentifier

internal actual fun createLocalizedDateFormatter(
    localeIdentifier: String,
    template: String,
): LocalizedDateFormatter {
    val formatter =
        NSDateFormatter().apply {
            locale = NSLocale(localeIdentifier = localeIdentifier)
            calendar = NSCalendar(calendarIdentifier = NSCalendarIdentifierGregorian)
            timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0)
            setLocalizedDateFormatFromTemplate(template)
        }
    return LocalizedDateFormatter { date: LocalDate ->
        formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(date.toEpochDays() * SECONDS_PER_DAY))
    }
}

private const val SECONDS_PER_DAY = 86_400.0
