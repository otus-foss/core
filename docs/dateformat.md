# Date formatting

The `dateformat` module (`com.otus.dateformat`, artifact `com.otus:dateformat`) formats dates for
display and stores the user's date-format preference. It has no UI. It moved here from otus-mail in
version 0.2.0.

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
`LocalDate`, ISO date strings, and ranges. `formatDayMonth` leaves out the year, for places where the
reader knows it, such as this year's mail. It has no Compose, feature, storage, or account dependencies.
Malformed ISO input is rejected; do not feed user-entered dates to this API without validation.

```kotlin
val formatter = DateFormatter(DateFormatPreference.DayMonthYearSlashes)
val label = formatter.format(LocalDate(2026, 10, 4)) // 04/10/2026
val short = formatter.formatDayMonth(LocalDate(2026, 10, 4)) // 04/10
```

## Preference storage

Android stores a stable choice ID in app-private SharedPreferences through
`AndroidDateFormatPreferenceStore(context)`; iOS uses NSUserDefaults through
`IosDateFormatPreferenceStore()`. Missing or unknown IDs fall back to device formatting. Selecting
DeviceLocale saves the automatic choice rather than the currently resolved pattern, so later phone
locale changes still apply. Each installed app has its own preference; nothing is shared between
apps.

## Validation

Common tests cover every choice with and without the year, ranges, and relative labels. `AndroidDateFormattingTest` runs
under Robolectric and covers locale regions, locale changes, and stored preferences. iOS adapters
run in the macOS CI job.

Platform references:
- [Android date pattern generation](https://developer.android.com/reference/android/text/format/DateFormat#getBestDateTimePattern(java.util.Locale,%20java.lang.String))
- [Apple localized date templates](https://developer.apple.com/documentation/foundation/dateformatter/setlocalizeddateformatfromtemplate(_:))
