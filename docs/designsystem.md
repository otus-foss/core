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
  `CalendarMonthBackground`, `Chip`, `IconButton`, `InputField`, `RefreshIndicator`,
  `SearchField`, `SearchPrompt`, `SelectionRow`, `SidebarEntry`, `Symbol`, and the
  `controlPressFeedback` modifier.
- `components`: `ActionDialog`, `ChoiceDialog`, `InputDialog`, `ContextPanel`, `DatePickerDialog`,
  `MonthNavigation`, `MonthYearPicker`, `PageScaffold`, `Subpage`, `PageSection`, `PanelSurface`,
  `PickerSearch`, `PullToRefresh`, `SelectionSheet`, `Sidebar`, and `SidebarSwipeArea`.
- Version 0.3.0 adds `DateRangePicker` for dates up to today, its continuous week calendar,
  and `AvatarStackChip` with `ChipAvatar` values. The app maps its senders or attendees into
  these values; the component has no contact lookup or feature dependencies.

All visual tokens live here, including `InboxTokens`. New elements and reusable components
belong here even when only Mail currently uses them. Their APIs use values and callbacks,
without app dependencies.

## Resources

The module bundles Figtree Regular with its SIL Open Font License and all Material Symbols
used by Mail, accounts, and settings with their Apache 2.0 license. The generated public `Res`
class lives in `com.otus.designsystem.resources`. Apps select icons from this bundle and pass
them to `Symbol` as `DrawableResource` values.

## Behavior guides

These guides still live in Mail, which was the first app to use the components:
[context panel](https://git.hilfikers.com/florian.ruby/otus-mail/src/branch/main/docs/context-panel.md),
[selection sheets](https://git.hilfikers.com/florian.ruby/otus-mail/src/branch/main/docs/selection-sheets.md),
[sidebar](https://git.hilfikers.com/florian.ruby/otus-mail/src/branch/main/docs/sidebar.md),
[motion](https://git.hilfikers.com/florian.ruby/otus-mail/src/branch/main/docs/motion.md), and
[color theme](https://git.hilfikers.com/florian.ruby/otus-mail/src/branch/main/docs/design.md).

## Tests

Common tests cover the continuous calendar week model and decoded-avatar cache, and an Android host test covers image decoding. The
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
