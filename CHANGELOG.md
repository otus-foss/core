# Changelog

## Unreleased

- Add `symbol_bookmark` (Material Symbols Rounded, Apache 2.0).
- Add `LabelPill`, a read-only pill with an optional icon, sized by `LabelTokens`. A highlighted
  label has an `accentRegular` border and `accentLight` text; others have a `bgLight` border and
  `text2` text. `ContentRowText` takes `labels: List<RowLabel>`, shown before the metadata text on
  the row's last line, which keeps a label's height whether or not it has labels. Empty metadata
  is left out. Existing calls compile unchanged.

## 0.5.0 - 2026-10-11

- Add `SwipeActions` and `SwipeAction` for list rows. Dragging a row toward either edge uncovers
  an `accentRegular` area that grows out of the row's edge: its body is the row's full height,
  rounded by 12 dp at its free end, and joined to the edge by concave 12 dp fillets that reach past
  the row into its neighbors. The swiped row draws above its neighbors while it moves. The action's icon grows
  and slides in beside its label, both clipped to that area. The row stops once its action is
  fully revealed. Releasing past 96 dp, marked by a haptic, flashes and runs the action once;
  drags cannot move the row again until it is still, so a touch on a returning row never runs
  it twice. Dismissing actions leave the row for the list to remove; others return it to rest.
  Both actions are also accessibility custom actions, and right-to-left layouts mirror the
  directions.
- Fix SemiBold panel text rendering at the regular weight. The bundled variable Figtree file is
  replaced by the static Regular, SemiBold, Bold, and Black files, and `panelTextStyle()` registers
  all four. With Android's bold text setting, Normal and SemiBold resolve to Bold and Black, so
  emphasized text still differs; before, both fell back to the same file. Unread
  `ContentRow` titles and the selected date wheel value now draw from the SemiBold file. Regular
  text is static Figtree Regular 400; measured on a Pixel 9a, it is lighter than the previous
  build, whose rendered weight did not match its declared one.
- Add flat settings rows inspired by Notion: `SettingRow` (title, description, and a trailing
  slot such as a chevron or value), `SettingToggleRow`, and `SettingsSection` with a large title.
  Rows are separated by `bgLight` hairlines instead of filled cards. New elements: `ToggleSwitch`
  and `ValueButton`, an outlined current value with a down chevron. Their sizes stay internal.
- Add `ConfirmDialog` for destructive changes: a title, a message about what happens and what is
  kept, and Cancel beside the confirming button. `DialogFrame` takes `closeButton = false` to
  leave out its close icon when the popup has its own Cancel. Removing an account in
  `AccountSetupScreen` now asks first.
- Add `ChoiceSheet`, a bottom sheet with a grab handle, centered title, and Done, with options in
  rounded groups and a check beside the selected one. It takes `ChoiceOptions(options, selected,
  default)`; a `default`, such as the device date format, is listed first in a group of its own.
  Choosing applies at once and keeps the sheet open. Done, the scrim, Back, or swiping down
  anywhere on the sheet slides it away; in the option list, the swipe takes over once the list is
  at its top. `AppearanceSettingsScreen` uses it for the date format.
- `SettingRow` takes a nullable `onClick`; without it the row is static and only its trailing
  controls act. New element: `RowButton`, a small outlined button for row actions such as Remove.
- `AccountSetupScreen` uses large section titles, and `ConnectedAccountRow` is a flat row with
  an outlined Remove button. Below the name it shows the username, unless the name already is
  the username because the server gave no display name, and then the server. Its accessible
  name stays "Remove <account>".
- `SettingsScreen` and `AppearanceSettingsScreen` use the new rows. The date format shows its
  pattern in a `ValueButton`, with "Device" for the device setting, and an example below its title.
- Add `symbol_check`, `symbol_flag`, `symbol_expand_more`, and `symbol_mark_email_read` (Material Symbols Rounded, Apache 2.0).
- `AppearanceSettingsScreen` takes an optional trailing `content` slot for app-owned sections
  below the shared date format. Existing calls compile unchanged.
- Remove `dateFormatSummary`. The date format row shows the pattern and the example separately,
  and nothing else called it.

## 0.4.0 - 2026-10-10

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
