# Changelog

## Unreleased

- Extract `SpecificDatePicker` as a reusable wheel picker with caller-supplied date bounds and
  a `dayRequired` option. Required mode disables Go until a day is chosen; optional mode allows
  Any. Keep `MonthYearPicker` as the optional-day calendar adapter.
- Restyle `MonthYearPicker` as a bottom sheet with centered, snapping date wheels and a full-width
  Go button. Its optional day starts at Any; month-only confirmation navigates without changing
  the calendar selection.
- Breaking API change: `MonthYearPicker.onConfirm` now receives `Int?` for the day, and
  `initialDay` is nullable with a null default. Consumers handle null as month-only navigation,
  or pass an explicit `initialDay` when they need a preselected date.

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
