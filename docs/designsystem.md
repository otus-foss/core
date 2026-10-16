# Design system

The `designsystem` module (`com.otus.designsystem`, artifact `com.otus:designsystem`) holds the
Otus theme, elements, and components. It moved here from otus-mail in version 0.2.0. Elements and
components take values and callbacks; they own no ViewModels, navigation, networking, or storage.
The user picks one of four themes and an accent color. Changing any single color saves a custom
theme. The Otus theme with Otus blue is the palette recorded in [mail design foundations](suite/mail-design.md). See
[decision 0014](decisions/0014-user-themes.md).

## Contents

- `theme`: `OtusColors`, `OtusTheme`, `OtusPalette`, `ThemeBase`, `ThemeChoice`, `ThemeColor`, `CustomTheme`, `AccentPreset`,
  `accentShades` (with `AccentShades`), color helpers (`hslColor`, `toHsl`, `contrast`, `toHex`,
  `parseHexColor`), `FolderColor`, `ThemeTokens`, `Motion` (with `MotionSpeed`, `MotionCurve`, and
  `LocalMotionScale`), and tokens for avatars, calendars, control
  feedback, pages, panels, refresh, sheets, the sidebar, the action bar (`ActionBarTokens`), and
  compose fields (`ComposeTokens`), and rule chips and folder settings rows (`RuleChipTokens`,
  `FolderRowTokens`).
  `panelTextStyle()` uses Figtree.
- `elements`: `ActionButton`, `ActionRow`, `AnimatedText`, `Avatar`, `AvatarImage`, `BarIconButton`, `CalendarDay`,
  `ComposeBody`, `ComposeField`, `ComposeValueField`,
  `CalendarMonthBackground`, `Chip`, `ColorSlider`, `ColorSwatch`, `AddSwatch`, `SwatchStack`, `IconButton`, `IconPill`, `InputField`, `LabelPill`, `RefreshIndicator`,
  `FolderColorSwatch`, `LocalIconTint`, `RowButton`, `SearchField`, `SearchPrompt`, `SelectionRow`, `SidebarEntry`, `SidebarHeading`,
  `SidebarCount`, `SidebarExpandButton`, `SidebarPill`, `SidebarInboxEntry`, `SidebarMailboxTile`,
  `SidebarFolderEntry` (with `SidebarFolder`), `SidebarDivider`, `SectionCaption`, `Symbol`,
  `ToggleSwitch`, `ValueButton`, and the
  `controlPressFeedback` modifier.
- `components`: `ActionBar`, `ActionBarHost`, `BarAction`, `BarContext`, `BarToken`, `ActionDialog`, `ChoiceDialog`, `ChoiceSheet` (with `ChoiceRow`), `ConfirmDialog`, `InputDialog`, `ContextPanel`, `DatePickerDialog`,
  `MonthNavigation`, `MonthYearPicker`, `PageScaffold`, `Subpage`, `PageSection`, `PanelSurface`,
  `PickerSearch`, `PreviewCard`, `PullToRefresh`, `ReorderableColumn`, `SelectionSheet`, `SheetFrame`, `SettingRow`,
  `SettingToggleRow`, `SettingsSection`, `SpecificDatePicker`, `Sidebar`, `SidebarHeader`, `SidebarMailboxGrid`, `ScopeSheet`, `ScopeRow` and `ScopeChildRow` (with `ScopeOption`), `ScopePicker`,
  `RuleChip`, `RuleChipGroup`, and `AddRuleChip` (with `RuleChipLine`, `RuleChipValue`, `RuleSegment`, and
  `RuleSegmentStyle`), `FolderSettingRow` with `FolderRuleSummary` and `FolderOwnerLine`, `SummaryCard` and `PersonText` (with `NamedAvatar`), `ValueToken`, `ValuePicker`, `Collapsible`, `CollapsibleTree`, `AvatarStack`, `ContextMenu` (with `MenuAction`),
  `SidebarSwipeArea`, `SlidingOverlay`, `SlidingPages`, `SwipeActions`, and `ThemeCard`.
- Settings rows follow Notion's settings pages: flat rows with a title and `text2` description
  beside the control, `bgLight` hairlines between them, and large section titles. The whole row is
  the control; `ToggleSwitch` and `ValueButton` only show its state. A long picker, such as Mail's
  folder lists, uses `RowDensity.Compact`: 36 dp per row and no hairlines. Folder lists, including
  `SidebarEntry` and `SidebarFolderEntry` rows, use `PanelTokens.compactRowHeight`, below the
  48 dp touch target by the user's choice. A row without a click is
  static, and a `RowButton` at its end acts on its own. Settings choices open a `ChoiceSheet`,
  which lists a `ChoiceOptions` default in its own group first, optionally with an icon per
  option, applies a choice at once, and closes with Done. Sheets are as tall as their content, up
  to 88% of the screen; a sheet with a weighted list, such as Mail's Move to sheet, fills that height.
  `ValueToken` shows a value in an outline, optionally after a keyword and an avatar or icon,
  such as "Move to" and a folder symbol before "Receipts". `ValuePicker` puts the same token, a
  thin divider, and a down chevron in one outline. The whole picker is one 48 dp button that calls
  `onClick`, and the app opens its choices there. Screen readers hear "Move to Receipts, change".
  A disabled picker uses `text3` and ignores taps.
  `IconPill` is an icon-only toggle that sits at the end of a row of `SidebarPill`s. Destructive settings actions ask with `ConfirmDialog` first; its confirming
  button uses the primary style because the palette has no warning color, which is an open design
  decision. `ChoiceDialog` and `ActionDialog` remain for one-shot picks
  and menus. Mail's rules pages still use `PageSection`
  and `ActionRow`.
- `FolderColor` lists the colors a user can give a folder: `Default` and eight named colors.
  Apps store the enum `name` and read it back with `FolderColor.named`, which returns `Default`
  for null or unknown names. `tint` is the icon color in the current theme, or null for
  `Default`. Each color has one shade for dark themes and one for light themes, chosen by
  `OtusPalette.isLight`, so custom themes use the shades of their kind. `FolderColorTest` checks
  that every shade reaches 3:1 against `bgDark`, `bgRegular`, and `accentDark` for every theme and
  accent preset. A new kind of theme adds a shade to each color in `FolderColor.kt`.
  The color tints only the folder icon. Provide it with `LocalIconTint` around a row:
  `SidebarEntry`, `ActionRow`, `SettingRow`, `SettingToggleRow`, `ValuePicker`, and `ValueToken`
  then draw their leading icon in it. A composition local keeps these signatures within the
  seven-parameter limit. Text, backgrounds, the selected fill, trailing icons, and a disabled
  picker's icon keep their colors. A `ChoiceRow` takes `iconTint` for its icon, and `swatch` shows
  a `FolderColorSwatch` before its label: a filled circle in the tint, or an empty `text2` ring for
  `Default`. The swatch is decoration; screen readers hear the option's label.
- The `Sidebar` is one panel, 336 dp wide, with a `header` slot over its scrolling content.
  `SidebarHeader` shows avatars, a title, and an optional subtitle, then an unfold button and
  Settings. Tapping the title or the unfold button calls `onPick`. Below it, `SidebarInboxEntry`
  is a 52 dp row for the main mailbox. `SidebarMailboxGrid` puts `SidebarMailboxTile`s in three
  equal columns; a tile shows a count badge on its icon's corner, and the caller shortens large
  counts. `SidebarDivider` separates groups, and `SectionCaption` titles a group that does not
  collapse, with an optional action at its end. A `SidebarFolderEntry` row is 36 dp tall, like
  `SidebarEntry`, and its icon toggle is 48 dp wide. A folder with subfolders uses its icon as the expand toggle, an open folder
  while expanded, and a long press calls `onLongClick`. Its row callback matches
  `CollapsibleTree`, and its icon takes `LocalIconTint`.
- `ScopeSheet` picks what a page shows, such as every account, one account, or one address. It
  takes an optional search slot, such as `PickerSearch`, and a footnote. `ScopeRow` is a 64 dp
  choice with 40 dp avatars. A row with a subtitle is a group with a SemiBold title, and passing
  `expanded` adds a chevron. `ScopeChildRow` is a 52 dp nested choice with 32 dp avatars, indented
  so its avatar lines up with the group's title. The chosen row gets an `accentDark` fill, and a
  pinned row shows a pin. A long press calls `onLongClick`, which the row names
  "Pin" or "Unpin" for screen readers. The caller wraps nested rows in `Collapsible` and owns what
  is chosen, pinned, and expanded.
- Version 0.3.0 adds `DateRangePicker` for dates up to today, its continuous week calendar,
  and `AvatarStackChip` with `ChipAvatar` values. `AvatarStack` draws the same avatars without
  the pill, such as the senders a rule files into a folder. The app maps its senders or attendees into
  these values; the component has no contact lookup or feature dependencies.
- `SwipeActions` hosts one list row with an optional `SwipeAction` for each direction. The app
  decides which actions a row offers and what they do; the component only reveals, confirms, and
  reports them. Its motion follows the Snepilatch track rows: a clipped icon and label that grow in
  until the threshold, a row that stops at the revealed width, and a short flash on commit. The
  uncovered area is an accent shape the row's full height, attached to the row's edge with
  inverted corners: concave fillets that reach past the row into its neighbors, and a rounded
  free end. The swiped row draws above its neighbors so they do not cover the fillets. Colors use `accentRegular`, `text1`, and
  `bgRegular`. Rows that should show it against the screen edge must reach that edge. A row inside `SidebarSwipeArea` keeps its swipes, because the sidebar opens only
  from the context panel. A release runs an action only when no other action of that row is
  running. A dismissing action leaves the row at the edge for its list to remove; if the row is
  still there one second later, for example because removal failed, it returns to rest.
- `ReorderableColumn` lets the user reorder a short list by dragging each row's handle. Only the
  handle starts a drag, so rows keep their own taps and the list around them still scrolls. The
  dragged row draws above the others and stays within the column. Each row is filled with
  `background`, `bgRegular` by default; pass `bgDark` inside a sheet so a moving row hides the rows
  beneath it. The rows it passes step aside
  at the fast speed, 160 ms, and a haptic marks each new place. The caller gets one `onMove(from, to)` on drop
  and owns the order. Each handle names its row for accessibility services and offers Move up and
  Move down. Give every row a unique, stable key. Changing the row order cancels an active drag.
  Every row is composed, so it suits settings lists, not message lists.
  A row without a handle passes `Modifier.dragOnLongPress()` from its `ReorderableScope`: a long
  press anywhere on the row starts the drag, and a tap still reaches the row's click. The gesture
  reads the initial pointer pass and consumes the rest of a long press, so lifting the finger does
  not click the row or scroll the list. The overload with `onNest` also takes drops onto a row,
  such as a folder dropped onto another folder. While the dragged row's leading edge, its bottom
  edge moving down or its top edge moving up, is in the middle half of another row, that row gets
  an `accentDark` fill and a drop calls `onNest(from, onto)`. In the outer quarters the drag
  reorders and calls `onMove`, and a row steps aside only once the leading edge passes its middle
  half. Screen readers get Move up and Move down but no way to nest; the app offers that elsewhere,
  such as in a folder's settings.
- `RuleChip` shows one condition of a mail rule in a `bgLight` outline, like `ValueToken`. Its
  grid is fixed so stacked chips line up: a 52 dp label column (12 sp SemiBold `text2`), a 20 dp
  slot, and the value (15 sp, one line, ellipsis). The slot holds a 20 dp avatar for people and
  domains, or an 18 dp `text2` icon for every other kind of condition. The app passes a
  `RuleChipLine` with a `RuleChipValue` made of `RuleSegment`s: `Dim` (`text3`) for how it matches,
  such as "starts with", `Bright` (`text1`) for what it matches, and `Accent` (`accentLight`) for a
  wildcard `*`. The app writes the words and quotes; the chip draws them as given. A full chip is
  36 dp tall. `RowDensity.Compact` makes a 24 dp chip for a folder row: its label takes only the
  width it needs, its avatar is 16 dp, and it leaves out the icon. `RuleChipGroup` draws an "All
  of" rule: one outline, a small `text3` title such as "ALL OF", and hairlines between its
  conditions, on the same grid. `AddRuleChip` is a dashed chip of the same height with its plus in
  the slot column and its label in the value column. A chip or group with `onClick` is one button.
- `FolderSettingRow` is one folder on a folder settings page: the folder icon, the name in 17 sp,
  an optional detail line, and a chevron, at least 56 dp tall and indented 20 dp per level. The
  icon expands and collapses subfolders like `SidebarFolderEntry`'s, shares its code, and takes
  `LocalIconTint`. `icon` replaces the closed folder, such as `symbol_folder_shared`. The detail
  is a slot: `FolderRuleSummary` shows the first rule as a compact chip and a `text3` count such as
  "+1", and `FolderOwnerLine` shows the owner's 16 dp avatar before text such as "Shared by
  Florian · can read". `ScopePicker` is a `bgDark` card with stacked avatars, a SemiBold title, a
  `text2` subtitle, and an unfold icon, such as "All accounts" over "2 accounts · 11 folders". The
  whole card is one button that opens the app's picker. It shares its avatars and text with
  `SidebarHeader`.
- `ActionBar` is a page's bottom bar: up to three icon-only secondary actions in a group, and the
  primary action in a shell beside it. Each secondary action is a 48 dp `BarIconButton` that
  shows its label on long press. A disabled action stays in its slot with a `text3` icon, so the
  buttons never move. Without a `BarContext`, the primary fills the shell with its label. With
  one, the primary shrinks to a 60 dp button at the shell's end, and a `BarToken` with the
  primary's label and the context's subtitle fills the rest. The whole shell stays one button,
  read as "label, description". A bar that opens over another moves its shell and action from
  the other's in one 160 ms motion on the standard curve. When the page closes, the bar beneath
  moves back on the reverse curve, so closing replays opening backward; a context that goes away
  in place leaves the same way. When the primary changes, including when a page opens over
  another or closes back to it, its icon and label fade and scale out while the new ones fade and
  scale in, 120 ms on the exit and enter curves; the shell stays. With `minimized`, the bar shows only the primary's icon in a
  62 dp circle at its end, as far from the screen's end edge as from its bottom edge (the
  navigation bar counts toward both), filled like the full primary. Its ring is `accentLight`. With a `BarContext`, a fully rounded
  `bgDark` shell with a `strokeBgDark` outline grows out from behind the circle toward the start,
  240 ms on the enter curve, and shrinks back behind it on the exit curve. The context follows the
  frontmost bar like the icon, so it grows when a selection starts and shrinks when it ends. The
  whole button runs the primary. It stays in place while the
  keyboard moves. With `hidden`, or when the keyboard starts opening, it fades and scales out like
  `PickerSearch`'s clear button, 120 ms on the exit curve. It fades and scales back in on the
  enter curve once the closing keyboard drops below it. Pass `hidden` as soon as a field will take
  the keyboard, because the keyboard otherwise covers the button within a few frames. When its primary changes, including when a page opens over another or closes
  back to it, or when a selection's bar replaces a list's, the old icon fades and scales out while the new one fades and scales in inside
  the circle, which stays, at the same speed and curves and read as "label, description"; the
  secondary actions are not drawn. The app chooses the actions and the context; the component only
  draws them and reports clicks. `ActionBarHost` places the bar over the bottom of a page, above
  the navigation bar and keyboard, and pads the page's content so it can scroll clear of the bar.
  Mail uses it for an open message and for selected messages; see
  [Mail's action bar guide](https://github.com/reazndev/otus-mail/blob/main/docs/action-bar.md).
- `ComposeField` and `ComposeBody` lay out a message being written. Each header line, such as
  To or Subject, puts a `text2` label beside a borderless field with a `bgLight` hairline below,
  like Gmail's and Apple Mail's compose headers. `ComposeValueField` shows a chosen value, such
  as the From address, and opens the choice when tapped. The body has no border and fills the
  rest of the page. The app owns the draft, parsing, and saving.

All visual tokens live here, including `InboxTokens`. New elements and reusable components
belong here even when only Mail currently uses them. Their APIs use values and callbacks,
without app dependencies.

## Motion

Every animation in the design system and in Mail takes its duration and curve from `Motion`, in
`theme/Motion.kt`. The rules for picking a speed and a curve are in
[mail design foundations](suite/mail-design.md#motion).

`MotionCurve` holds four cubic Bézier curves:

| Curve | Bézier | Use |
| --- | --- | --- |
| `Standard` | (0.4, 0, 0.2, 1) | The default, for anything that changes in place |
| `Reverse` | (0.8, 0, 0.6, 1) | `Standard` played backward, for undoing a change so it retraces its steps |
| `Enter` | (0.2, 0, 0, 1) | Decelerates, for sheets, the sidebar, and text that arrive |
| `Exit` | (0.4, 0, 1, 1) | Accelerates, for the same things as they leave |

`MotionSpeed` divides the 240 ms base duration (`Motion.BASE_MILLIS`) by its factor:

| Speed | Factor | Duration | Use |
| --- | --- | --- | --- |
| `Quick` | 2 | 120 ms | Press feedback, the refresh indicator |
| `Fast` | 1.5 | 160 ms | Toggle switch, reorder shift, action bar collapse, outgoing text |
| `Normal` | 1 | 240 ms | Panel modes, sheets, pages sliding, row selection, swipe flash, incoming text |
| `Relaxed` | 0.8 | 300 ms | Sidebar opening, a swiped row settling |

A duration is `240 ms / (factor × scale)`, rounded to the nearest millisecond. The scale comes
from `LocalMotionScale`, which defaults to 1. Providing 2 runs every animation twice as fast, and
0.5 runs it at half speed. Android's animator duration scale, set in the developer options or by
the system's remove animations setting, applies on top of the result, because Compose reads it
for every animation.

Apps provide the user's `AnimationSpeed` as the scale. The Animations row on the shared
appearance page sets it through `AnimationSettingsState`: Slow is 0.75, Regular 1, Fast 1.5, and
Instant is infinite, so every duration rounds to 0 ms.

A composable reads the scale for you:

```kotlin
val thumb by animateDpAsState(target, motionTween(MotionSpeed.Fast))
```

Transition lambdas, such as `AnimatedContent`'s `transitionSpec`, are not composable. Read the
scale outside the lambda and pass it in:

```kotlin
val motionScale = LocalMotionScale.current
AnimatedContent(
    state,
    transitionSpec = { fadeIn(Motion.tween(scale = motionScale)) togetherWith fadeOut(Motion.tween(scale = motionScale)) },
) { ... }
```

State holders that animate outside composition, such as `SidebarState`, the sheet position, and
the swipe settle, receive the scale from the composable that creates or drives them, and call
`Motion.tween(speed, curve, scale)` or `Motion.millis(speed, scale)`. The sidebar, for example,
shortens its tween to the distance left after a partial swipe:

```kotlin
val millis = Motion.millis(MotionSpeed.Relaxed, motionScale)
val spec = TweenSpec<Float>((millis * remaining).roundToInt(), easing = MotionCurve.Enter.easing)
```

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

Common tests cover the continuous calendar week model, decoded-avatar cache, motion durations, reorder behavior,
and sheet animation interruption and completion. An Android host test covers image decoding. The
Compose UI tests for panels, pickers, motion, and press feedback still run in Mail's `androidApp`,
because they need an Android activity. Moving them here needs a host-test setup with an activity
and is open work.

## Central UI ownership

Meta owns every branded element, reusable component, visual token, and symbol resource.
`ContentRow` renders `ContentRowText`, containing the title, subtitle, trailing text, and
metadata, with caller-supplied accessibility state and a
leading slot. A `selected` row is tinted `accentDark` and shows a check in place of the leading
slot; `onLongClick`, when given, usually starts selecting. `DetailRow` renders caller-supplied details with optional tree indentation.
`EmptyState` renders caller-supplied text and icon with an optional action. These APIs take
values and callbacks; Mail domain models and workflow decisions stay in Mail adapters.
`InboxTokens` preserves the existing message-list and empty-state sizes.

Import symbols through the public `com.otus.designsystem.resources.Res` bundle. Mail, settings,
and accounts use the same symbol files. The palette and existing visual sizes are unchanged.
See [decision 0008](decisions/0008-central-ui-ownership.md).
