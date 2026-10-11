# Design system

The `designsystem` module (`com.otus.designsystem`, artifact `com.otus:designsystem`) holds the
Otus theme, elements, and components. It moved here from otus-mail in version 0.2.0. Elements and
components take values and callbacks; they own no ViewModels, navigation, networking, or storage.
Dark mode is the only color mode. The palette is recorded in
[mail design foundations](suite/mail-design.md).

## Contents

- `theme`: `OtusColors`, `OtusTheme`, `MotionTokens`, and tokens for avatars, calendars, control
  feedback, pages, panels, refresh, sheets, and the sidebar. `panelTextStyle()` uses Figtree.
- `elements`: `ActionButton`, `ActionRow`, `AnimatedText`, `Avatar`, `AvatarImage`, `CalendarDay`,
  `CalendarMonthBackground`, `Chip`, `IconButton`, `InputField`, `LabelPill`, `RefreshIndicator`,
  `RowButton`, `SearchField`, `SearchPrompt`, `SelectionRow`, `SidebarEntry`, `Symbol`,
  `ToggleSwitch`, `ValueButton`, and the
  `controlPressFeedback` modifier.
- `components`: `ActionDialog`, `ChoiceDialog`, `ChoiceSheet`, `ConfirmDialog`, `InputDialog`, `ContextPanel`, `DatePickerDialog`,
  `MonthNavigation`, `MonthYearPicker`, `PageScaffold`, `Subpage`, `PageSection`, `PanelSurface`,
  `PickerSearch`, `PullToRefresh`, `ReorderableColumn`, `SelectionSheet`, `SheetFrame`, `SettingRow`,
  `SettingToggleRow`, `SettingsSection`, `SpecificDatePicker`, `Sidebar`, `SidebarSwipeArea`, and
  `SwipeActions`.
- Settings rows follow Notion's settings pages: flat rows with a title and `text2` description
  beside the control, `bgLight` hairlines between them, and large section titles. The whole row is
  the control; `ToggleSwitch` and `ValueButton` only show its state. A row without a click is
  static, and a `RowButton` at its end acts on its own. Settings choices open a `ChoiceSheet`,
  which lists a `ChoiceOptions` default in its own group first, applies a choice at once, and
  closes with Done. Destructive settings actions ask with `ConfirmDialog` first; its confirming
  button uses the primary style because the palette has no warning color, which is an open design
  decision. `ChoiceDialog` and `ActionDialog` remain for one-shot picks,
  such as moving a message, and menus. Mail's rules pages still use `PageSection`
  and `ActionRow`.
- Version 0.3.0 adds `DateRangePicker` for dates up to today, its continuous week calendar,
  and `AvatarStackChip` with `ChipAvatar` values. The app maps its senders or attendees into
  these values; the component has no contact lookup or feature dependencies.
- `SwipeActions` hosts one list row with an optional `SwipeAction` for each direction. The app
  decides which actions a row offers and what they do; the component only reveals, confirms, and
  reports them. Its motion follows the Snepilatch track rows: a clipped icon and label that grow in
  until the threshold, a row that stops at the revealed width, and a short flash on commit. The
  uncovered area is an accent shape the row's full height, attached to the row's edge with
  inverted corners: concave fillets that reach past the row into its neighbors, and a rounded
  free end. The swiped row draws above its neighbors so they do not cover the fillets. Colors use `accentRegular`, `text1`, and
  `bgRegular`. Rows that should show it against the screen edge must reach that edge. A row inside `SidebarSwipeArea` keeps its swipes, because the sidebar opens only
  from the context panel.
- `ReorderableColumn` lets the user reorder a short list by dragging each row's handle. Only the
  handle starts a drag, so rows keep their own taps and the list around them still scrolls. The
  dragged row draws above the others and stays within the column; the rows it passes step aside
  over 180 ms, and a haptic marks each new place. The caller gets one `onMove(from, to)` on drop
  and owns the order. Each handle names its row for accessibility services and offers Move up and
  Move down. Give every row a unique, stable key. Changing the row order cancels an active drag.
  Every row is composed, so it suits settings lists, not message lists.

All visual tokens live here, including `InboxTokens`. New elements and reusable components
belong here even when only Mail currently uses them. Their APIs use values and callbacks,
without app dependencies.

## Resources

The module bundles the static Figtree Regular, SemiBold, Bold, and Black files from the upstream
Figtree project with their SIL Open Font License. `panelTextStyle()` registers all four, so each
weight draws from its own file. Bold and Black cover Android's bold text setting, which adds 300 to
every requested weight: Normal and SemiBold become Bold and Black, and unread text stays heavier. A single variable file cannot serve both
weights on Android: Compose caches asset fonts by path, so every weight would draw from whichever
variation loaded first. The variable file it replaces defaulted to Light 300. It also bundles all Material Symbols
used by Mail, accounts, and settings with their Apache 2.0 license. The generated public `Res`
class lives in `com.otus.designsystem.resources`. Apps select icons from this bundle and pass
them to `Symbol` as `DrawableResource` values.

## Behavior guides

These guides still live in Mail, which was the first app to use the components:
[context panel](https://github.com/reazndev/otus-mail/blob/main/docs/context-panel.md),
[selection sheets](https://github.com/reazndev/otus-mail/blob/main/docs/selection-sheets.md),
[sidebar](https://github.com/reazndev/otus-mail/blob/main/docs/sidebar.md),
[motion](https://github.com/reazndev/otus-mail/blob/main/docs/motion.md), and
[color theme](https://github.com/reazndev/otus-mail/blob/main/docs/design.md).

## Date chooser

Version 0.4.0's `SpecificDatePicker` and `MonthYearPicker` components share one bottom sheet with
centered, snapping wheels, fading adjacent options, a circular close control, and a full-width
Go button. They use the supplied travel-date screenshot for layout and the existing Otus palette.
Both take an initial `YearMonth`, an optional initial day, an inclusive `allowedDates` range, and
dismiss/confirm callbacks. Future dates are allowed when the caller's range includes them.
Mail's calendar supplies January 1970 through today.

`SpecificDatePicker` always needs a day. It starts with Pick, disables Go until the user chooses
a day, and confirms with a `LocalDate`. An explicit initial day is also accepted and clamped to
the bounds. `MonthYearPicker` makes the day optional. It starts with Any and confirms with the
chosen `YearMonth` and a `LocalDate?` that is null when Any is selected. Changing month or year
preserves an unselected day. Selected days clamp to the valid month and both date bounds.
Closing, outside-tapping, or Back discards chooser edits.

Mail's calendar uses `MonthYearPicker`. Confirmation without a day navigates to the month and
preserves its current range; an explicit day retains start/end selection behavior.
The continuous calendar layout and gestures remain unchanged.

## Tests

Common tests cover the continuous calendar week model, decoded-avatar cache, reorder behavior,
and sheet animation interruption and completion. An Android host test covers image decoding. The
Compose UI tests for panels, pickers, motion, and press feedback still run in Mail's `androidApp`,
because they need an Android activity. Moving them here needs a host-test setup with an activity
and is open work.

## Central UI ownership

Meta owns every branded element, reusable component, visual token, and symbol resource.
`ContentRow` renders `ContentRowText`, containing the title, subtitle, trailing text, and
metadata, with caller-supplied accessibility state and a
leading slot. `DetailRow` renders caller-supplied details with optional tree indentation.
`EmptyState` renders caller-supplied text and icon with an optional action. These APIs take
values and callbacks; Mail domain models and workflow decisions stay in Mail adapters.
`InboxTokens` preserves the existing message-list and empty-state sizes.

Import symbols through the public `com.otus.designsystem.resources.Res` bundle. Mail, settings,
and accounts use the same symbol files. The palette and existing visual sizes are unchanged.
See [decision 0008](decisions/0008-central-ui-ownership.md).
