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

Tokens that only one app's features use stay in that app, such as Mail's `InboxTokens`. Add a new
token, element, or component here only when it has no app knowledge.

## Resources

The module bundles Figtree Regular with its SIL Open Font License, and the Material Symbols
`close`, `chevron_left`, and `search` icons with their Apache 2.0 license. The generated `Res`
class is internal (`com.otus.designsystem.resources`). Apps bundle their own icons and pass them to
`Symbol` as `DrawableResource` values.

## Behavior guides

These guides still live in Mail, which was the first app to use the components:
[context panel](https://github.com/reazndev/otus-mail/blob/main/docs/context-panel.md),
[selection sheets](https://github.com/reazndev/otus-mail/blob/main/docs/selection-sheets.md),
[sidebar](https://github.com/reazndev/otus-mail/blob/main/docs/sidebar.md),
[motion](https://github.com/reazndev/otus-mail/blob/main/docs/motion.md), and
[color theme](https://github.com/reazndev/otus-mail/blob/main/docs/design.md).

## Tests

Common tests cover the decoded-avatar cache, and an Android host test covers image decoding. The
Compose UI tests for panels, pickers, motion, and press feedback still run in Mail's `androidApp`,
because they need an Android activity. Moving them here needs a host-test setup with an activity
and is open work.
