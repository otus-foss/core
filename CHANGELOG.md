# Changelog

## Unreleased

- Resolve JMAP redirects and endpoint URLs using RFC 3986 rules, so discovery queries and URL
  credentials do not carry over to another host.
- Reject `/set` responses missing `newState`, submitted object outcomes, or created object IDs,
  and reject conflicting success/error outcomes while accepting additional server updates.
- Refresh cached JMAP sessions after failed blob transfers without automatically replaying uploads.
  A late failure from an older session no longer invalidates its replacement.
- Convert custom decoder cast and missing-key failures to `JmapException.NotJmap`; preserve cancellation.
- Breaking API change for 0.4.0: `QueryResult.position` and `QueryResult.total` use `Long` and
  `Long?` instead of `Int` and `Int?`, with JMAP's unsigned 53-bit bounds. Consumers must rebuild,
  use long counters, and explicitly check bounds before converting to an `Int`.
- Search contact-photo matches across pages of 32 candidates, restricted to readable subscribed
  address books. Stop on exhausted or changed results, stalled pagination, or a 10-second deadline.
- Add `SpecificDatePicker` as a reusable wheel picker for one date within a caller-supplied
  `allowedDates` range. Go stays disabled until a day is chosen, and `onConfirm` receives a
  `LocalDate`.
- Restyle `MonthYearPicker` as a bottom sheet with centered, snapping date wheels and a full-width
  Go button. Its optional day starts at Any; month-only confirmation navigates without changing
  the calendar selection.
- Breaking API change: `MonthYearPicker` takes an initial `YearMonth` and an `allowedDates` range
  instead of separate year, month, and `maxDate` values. `onConfirm` receives `(YearMonth,
  LocalDate?)`, with a null date when the day is left as Any. `initialDay` defaults to null.

## 0.3.1 - 2026-10-10

- Publish the unchanged 0.3.0 library sources to Forgejo Packages. Apps can move from 0.3.0
  without code changes.

## 0.3.0 - 2026-10-08

- Centralize all Mail symbols and `InboxTokens` in `designsystem`; export its public resource
  bundle. Consumers import `com.otus.designsystem.resources` and `com.otus.designsystem.theme.InboxTokens`.
- Add value-driven `ContentRow`, `DetailRow`, and `EmptyState`. Mail maps message, folder, rule,
  and empty-state values into these components without moving feature behavior.

- Add `com.otus:settings` and `com.otus:accounts`, extracted from Mail. Settings takes
  app-provided entries and navigation callbacks; account setup takes the required JMAP service
  and app-private login storage.
- Move Mail's historical date-range picker and its calendar helpers into `designsystem`,
  exposing `DateRangePicker`. Add `AvatarStackChip` and `ChipAvatar` for overlapping avatar pills.
- Allow Compose in the presentation layer of shared settings and account features, with
  architecture checks and regression tests for their boundaries. See decision 0007.

## 0.2.1 - 2026-10-08

- Publish `com.otus:designsystem`, which failed in 0.2.0, together with the other modules. The
  code is unchanged from 0.2.0. See decision 0006.
- Build every publication before uploading any, and check publications in CI.

## 0.2.0 - 2026-10-08

Incomplete release: `jmap`, `dateformat`, and `avatar` were uploaded, `designsystem` was not. Use
0.2.1.

- Add `com.otus:dateformat`, `com.otus:avatar`, and `com.otus:designsystem`, moved from otus-mail.
  Packages change from `com.otus.mail.core.dateformat`, `com.otus.mail.core.avatar`, and
  `com.otus.mail.designsystem` to `com.otus.dateformat`, `com.otus.avatar`, and
  `com.otus.designsystem`.
- Make the panel, sheet, calendar, and page tokens, `panelTextStyle`, `controlPressFeedback`, and
  `EmptyAvatarStore` public, because Mail's features use them.
- Add `AvatarTokens.size` for the default avatar size, replacing Mail's `InboxTokens.avatarSize`
  in the shared `Avatar` element.

## 0.1.0 - 2026-10-08

- Add the repository build, quality gate, suite documentation, and publishing to GitHub Packages.
- Add `com.otus:jmap`, moved unchanged from otus-mail commit 507b5cb: the JMAP client, typed
  `/get`, `/query`, `/changes`, and `/set` calls, blob uploads and downloads, and login storage.
