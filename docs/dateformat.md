# Date formatting

The `dateformat` module (`com.otus.dateformat`, artifact `com.otus:dateformat`) formats dates for
display. It has no UI and stores nothing: the `settings` module keeps the user's choice. It moved
here from otus-mail in version 0.2.0.

## Choices and defaults

`DateFormatPreference.DeviceLocale` is the default. Platform locale data determines the numeric
date order, separators, and digits. Language and region both matter, so en-US and en-GB differ.
Dates use the Gregorian calendar and four-digit years, matching `kotlinx.datetime.LocalDate`.
Explicit choices use Latin digits and zero-padded day and month values.

| Choice | Example for 4 October 2026 | Without the year |
| --- | --- | --- |
| Device locale | 10/04/2026 for en-US, 04/10/2026 for en-GB, 04.10.2026 for de-DE | The locale's day and month, such as 10/04 for en-US |
| DD.MM.YYYY | 04.10.2026 | 04.10 |
| DD/MM/YYYY | 04/10/2026 | 04/10 |
| MM.DD.YYYY | 10.04.2026 | 10.04 |
| MM/DD/YYYY | 10/04/2026 | 10/04 |
| YYYY-MM-DD | 2026-10-04 | 10-04 |
| YYYY/MM/DD | 2026/10/04 | 10/04 |

Android uses `DateFormat.getBestDateTimePattern`; iOS uses
`NSDateFormatter.setLocalizedDateFormatFromTemplate`. Both receive a template, a Gregorian
calendar, and UTC: `yyyyMMdd` for full dates and `MMdd` for dates without the year. The `MMdd`
formatter is created on first use. The input is a date, not an instant, so the device time zone must
not shift it to another day. Formatters capture their locale when constructed. App wiring caches
them by preference and locale identifier and checks the locale when it recomposes. Android locale
configuration changes recreate the activity. A caller retaining a formatter must recreate it if
the locale changes.

## API

`DateFormatter` accepts a `DateFormatPreference` and an optional locale identifier. It formats
`LocalDate` values and ranges. `formatDayMonth` leaves out the year, for places where the
reader knows it, such as this year's mail. It has no Compose, feature, storage, or account dependencies.
Malformed ISO input is rejected; do not feed user-entered dates to this API without validation.

```kotlin
val formatter = DateFormatter(DateFormatPreference.DayMonthYearSlashes)
val label = formatter.format(LocalDate(2026, 10, 4)) // 04/10/2026
val short = formatter.formatDayMonth(LocalDate(2026, 10, 4)) // 04/10
```

### Names and times

Labels that read better with words than numbers come from here too, so both apps name dates the
same way:

- `formatNamedDay(date, withYear)` writes the day with the short English month, such as `Oct 8`,
  `8 Oct`, `Oct 8, 2026`, or `8 Oct 2026`. The day comes first for a day-first format, and for the
  device locale when its numeric day-month order puts the day first, such as en-GB or de-DE.
- `shortWeekday(day)` writes `Mon`; `clockTime(time)` writes the 24-hour time with the hour
  unpadded, such as `8:05`.
- `formatRelative` writes `Today` and `Yesterday`, and `formatRecentDay(date, today)` writes
  `Yesterday` or the weekday of the five days before it, as mail lists name recent days.

Names are English in every locale for now.

### In Compose

`settings` holds `rememberDateFormatter(settings)`, which builds a formatter again only when the
choice or the locale changes, and `LocalDateFormatter`, which an app provides around its pages.
`dateformat` stays free of Compose.

## Preference storage

`DateFormatSettingsState` in `settings` keeps the choice in a `StoredChoice` from `storage`, as a
stable choice ID. Missing or unknown IDs fall back to device formatting. Selecting DeviceLocale
saves the automatic choice rather than the currently resolved pattern, so later phone locale
changes still apply. Each installed app has its own preference; nothing is shared between apps.
Mail keeps the file `otus_date_format` and key `date_format` on Android, and `otus.date_format`
on iOS, as before 2026-11-06.

## Validation

Common tests cover every choice with and without the year, ranges, relative labels, and names
(`DateNamesTest`). `AndroidDateFormattingTest` runs under Robolectric and covers locale regions
and locale changes. iOS adapters
run in the macOS CI job.

Platform references:
- [Android date pattern generation](https://developer.android.com/reference/android/text/format/DateFormat#getBestDateTimePattern(java.util.Locale,%20java.lang.String))
- [Apple localized date templates](https://developer.apple.com/documentation/foundation/dateformatter/setlocalizeddateformatfromtemplate(_:))
