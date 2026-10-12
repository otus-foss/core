# Changelog

## Unreleased

- Add user themes (decision 0014). `ThemeBase` has four themes: `Otus`, `Light`, `Graphite`, and
  `Black`. `AccentPreset` has seven accents, and `ThemeChoice(base, accent).palette` builds an
  `OtusPalette`. `accentShades` derives an accent's fill, outline, and text shades and keeps text
  readable at 4.5:1 on each theme. Otus with Otus blue keeps the original palette exactly.
- **Breaking:** `OtusColors` tokens are now getters that follow the current palette, and
  `OtusTheme` takes a `palette`, `OtusPalette.Otus` by default. Apps pass their
  `ThemeSettingsState.palette`. Code that stored a token in a property must read it when drawing
  instead. `CalendarTokens.monthBackground` and `alternateMonthBackground` are now getters.
- Add `OtusColors.scrim`; sheets, dialogs, and the sidebar dim the page with it, so the scrim
  stays dark in the Light theme.
- Add the color helpers `hslColor`, `toHsl`, `contrast`, `toHex`, and `parseHexColor`, and
  `ThemeTokens`.
- Add `ColorSwatch`, `AddSwatch`, `ColorSlider`, and `ThemeCard`, and the symbols
  `symbol_palette`, `symbol_menu`, `symbol_swipe_left`, `symbol_swipe_right`, `symbol_view_agenda`,
  `symbol_check_circle`, `symbol_calendar_month`, and `symbol_collapse_content` (Material Symbols
  Rounded, Apache 2.0).
- `ValueButton` drops its down chevron and takes an optional `swatch` color shown as a dot
  before the value.
- Add `ThemeSettingsState`, `ThemePreferenceStore`, and `ThemeSettingsScreen` to `settings`.
- **Breaking:** `AppearanceSettingsScreen` takes `theme` and `onTheme` and opens with a Look
  section: a Theme row, the app's rows from the new `look` slot, and the date format. The Dates
  section is gone. Apps must pass the new arguments.
- Fix a crash on Android in every JMAP upload and download, such as saving Sieve rules. URI
  templates now escape the closing brace, which Android's ICU regex engine requires.
- `PreviewCard` hides its preview from screen readers only when the card is a button, so a
  preview without `onClick` keeps its controls reachable.
- A minimized `ActionBar` with a `BarContext` shows it in a fully rounded shell that grows out
  from behind the round button toward the start and shrinks back on the bezier curves; the whole
  button runs the primary. The round button's ring is `accentLight`.
- The full `ActionBar`'s primary swaps its icon and label the same way when it changes, including
  across pages; the shell stays.
- The minimized `ActionBar` button sits as far from the screen's end edge as from its bottom edge.
  It stays in place while the keyboard moves, and fades and scales out like `PickerSearch`'s clear
  button with the new `hidden` parameter or as the keyboard opens. It returns once the closing
  keyboard drops below it.
  When its primary changes, including when a page opens over another or closes back to it,
  the old icon scales and fades out while the new one scales and fades in; the circle stays.
- `ActionBar` takes `minimized`, which shows only the primary's icon in a 62 dp circle at the
  bar's end and leaves out the secondary actions.
- `ReorderableColumn` fills each row with `background`, `bgRegular` by default, so a dragged
  row no longer shows the rows and dividers beneath it. Pass `bgDark` inside a sheet.
- Add `PreviewCard`, a raised `bgRegular` card that sketches a page with a `SketchMarker` and
  shows a preview below, such as an action bar. With a title it has a header, and with `onClick`
  the whole card is one button whose header ends in a chevron.
- `Sidebar` is 336 dp wide: a 64 dp account rail and a 272 dp panel. On a narrower screen the
  panel shrinks so at least 56 dp of the page stays visible. The 64% width rule, kept between
  264 and 384 dp, is gone. Opening it clears focus and hides the software keyboard. It takes `subtitle` and a `rail` slot, its `entries` slot is
  now `content`, and the `footer` slot is gone. The
  header shows a 16 sp SemiBold title over a 14 sp `text2` subtitle. Callers must pass the new
  arguments.
- Add `SidebarRail`, which scrolls its items and pins a `text2` Settings button to its bottom, and
  the rail elements `SidebarRailAccount`, `SidebarRailIcon`, `SidebarRailAdd`, and
  `SidebarRailDivider`. The selected item has an `accentDark` fill and a 2 dp `accentLight`
  outline; badges are 20 dp `accentDark` pills.
- `SidebarHeading` takes `expanded`, `onExpandedChange`, and an optional end `action`. Tapping it
  turns its chevron 90 degrees and collapses the group. Screen readers announce a heading button
  that is expanded or collapsed. Calls with only a text no longer compile.
- `SidebarEntry` replaces `badge` with a `trailing` slot. Pass `SidebarCount` for a 14 sp `text2`
  count, and `SidebarExpandButton`, a chevron button 48 dp wide and one row tall, for an entry
  with nested entries. Its row is 36 dp tall (`PanelTokens.compactRowHeight`) with no gap to the
  next entry, and its name uses the content color.
- Add `SidebarPill`, a chip for a built-in mailbox with a `bgRegular` fill and an optional 14 sp
  `text2` count, sized by `PanelTokens.countSize`.
- `Avatar` takes `fill`, which replaces the `colorIndex` fill.
- `SettingToggleRow` takes an optional `icon`, as `SettingRow` does.
- Add the `symbol_all_inbox` Material Symbol.
- Add `symbol_auto_filter`, a funnel with a sparkle, for rules that sort mail automatically.
- Breaking: `ChoiceSheet` takes `row: (T) -> ChoiceRow` in place of `label` and `detail`. Replace
  `label = { it.name }, detail = { it.hint }` with `row = { ChoiceRow(it.name, it.hint) }`.
  `ChoiceRow` also takes an optional `icon`, a `person` whose avatar goes before their name, and a
  `keyword`, which shows the label as a `ValueToken`.
- `SheetFrame` is as tall as its content, up to 88% of the screen; content with a weighted child
  still fills that height. It takes `doneLabel`, such as "Undo", for its trailing action.
- Add `ValueToken`, a value in an outlined box with an optional keyword and an avatar or icon, and
  `ValuePicker`, which adds a divider and a down chevron and calls `onClick` on tap. The picker is
  48 dp tall, reads as one button such as "Move to Receipts, change", and takes `enabled`. Give it
  `Modifier.fillMaxWidth()` to stretch the outline, with the chevron at the end.
- Add `PersonText`, which puts a person's small avatar before their name in a line of text, and a
  `SettingRow` variant whose description names a `person` that way.
- Add `IconPill`, a round icon-only toggle that sits beside `SidebarPill`s.
- Breaking: `Motion`, `MotionSpeed`, `MotionCurve`, and `LocalMotionScale` replace
  `MotionTokens`, and every animation in the design system now uses them.
  `Motion.millis(speed, scale)` returns `240 ms / (factor × scale)`, rounded.
  `Motion.tween(speed, curve, scale)` builds a tween for code outside composition, and the
  composable `motionTween(speed, curve)` applies `LocalMotionScale` itself. `LocalMotionScale`
  defaults to 1; providing 2 makes every animation twice as fast, and Android's animator duration
  scale still applies on top. See [the design system guide](docs/designsystem.md#motion).
- Breaking: removed `MotionTokens` and `PanelTokens.MODE_TRANSITION_MILLIS`. To migrate, replace
  `MotionTokens.standard`, `enter`, and `exit` with `MotionCurve.Standard.easing`, `Enter.easing`,
  and `Exit.easing`, or better, replace the whole `tween(...)` with `motionTween(speed, curve)`.
  Replace `tween(PanelTokens.MODE_TRANSITION_MILLIS)` with `motionTween()`.
  `MotionTokens.TEXT_REPLACEMENT_MILLIS` becomes `MotionSpeed.Normal` with `MotionCurve.Enter`,
  and `TEXT_EXIT_MILLIS` becomes `MotionSpeed.Fast` with `MotionCurve.Exit`.
- Durations moved to the nearest speed. Control press feedback and the refresh indicator's
  crossfade go from 100 to 120 ms. The toggle switch goes from 150 to 160 ms. The reorder shift
  and outgoing text go from 180 to 160 ms. Panel mode changes, the sidebar's close, and the
  section chevron's turn go from 220 to 240 ms. The sheet's open goes from 250 to 240 ms, and incoming text from 260 to 240 ms. The
  sheet's close, row selection, and the swipe flash go from 200 to 240 ms, the largest change.
  The sidebar's open and a swiped row's settle stay at 300 ms.
- The refresh indicator's crossfade and its arming fade, which used Compose's default curve or
  spring, now use the standard curve at `Quick` speed.
- `SidebarState` takes the motion scale from the `Sidebar` that shows it, and `SheetFrame` and
  `SwipeActions` read it when they start. A `SidebarState` used without a `Sidebar` animates at
  scale 1.

- Add `ActionBar(primary, modifier, secondary, context)`, the bottom bar of a page. Up to three
  `secondary` `BarAction`s sit icon-only in a group, each a 48 dp `BarIconButton` that shows its
  label in a tooltip on long press. A disabled action keeps its slot and turns its icon `text3`.
  The `primary` action fills a shell beside the group and shows its label. With a `BarContext`,
  the action shrinks to a 60 dp button at the shell's end, and the context fills the rest: a
  `BarToken`, the primary's label, and the context's subtitle. The whole shell is then one button
  that accessibility services read as "label, description". A disabled primary has a `bgDark`
  fill, a `strokeBgDark` border, and `text3` content, so no accent suggests it can be pressed.
  More than three secondary actions throw.
- `BarToken` draws what the context is about: `Person`, `People(first, count)` (an avatar and the
  number of others), `Count`, or `Date(month, day)`. The width animation runs at `MotionSpeed.Fast`, 160 ms, quicker than the spec's 220 ms, with the
  standard curve. The label fades out over its first 40%, and the context fades in over the rest.
  A changed token, label, or subtitle crossfades in at `Normal` speed and out at `Fast` speed,
  like `AnimatedText`.
- Add `ActionBarHost(bar, modifier, content)`. It places the bar 8 dp from the screen edges, above
  the navigation bar and the keyboard, and passes `content` bottom padding equal to the bar's
  height so the last item can scroll above it. A null `bar` adds no padding.
- Add `ActionBarTokens`. The 62 dp height and the 60 dp collapsed action width come from the
  supplied spec. The other sizes were measured from the spec's U2 image and are not verified
  Figma values; see [mail design foundations](docs/suite/mail-design.md#action-bar).
- `ContentRow` takes `selection` and `onLongClick`. A selected row gets an `accentDark` tint,
  rounded by the new `InboxTokens.selectionRadius` (22 dp), and an `accentRegular` circle with a
  check in place of its leading content. `selection` is null while the list is not selecting, so
  accessibility services announce selection state only during a selection. Its
  `RowSelection.joinedAbove` and `joinedBelow` square the corners a selected neighbor touches, so
  adjacent selected rows read as one block. The tint fades and the corners reshape at `Normal`
  speed with the standard curve. Both
  parameters come before `leading`, so named-argument and trailing-lambda calls compile
  unchanged. Calls that passed `leading` as the fifth positional argument must use `leading =`.
- Add `ComposeField(label, value, onValueChange, modifier, kind, trailing)`, a header line of a
  message being written: a `text2` label beside a borderless single-line field, at least 48 dp
  tall, with a `bgLight` hairline below. The field names itself with the label, and `trailing`
  holds a control for the line. `ComposeValueField(label, value, onClick, modifier)` shows a
  chosen value, such as the From address, as one button with a down chevron, or as static text
  when `onClick` is null.
- Add `ComposeBody(value, onValueChange, placeholder, modifier)`, the borderless multi-line text
  of a message. It fills the space the caller gives it, takes taps anywhere in it, and shows the
  placeholder in `text3` while empty.
- Add `ComposeTokens` for the label gap and hairline width. No Figma frame covers compose yet, so
  both are app decisions.
- Add `symbol_more_horiz`, drawn from the Material Symbols Rounded `more_horiz` glyph geometry
  (Apache 2.0).
- `AvatarService` fetches and caches website icons only for the sender's registrable domain from
  the injected `AvatarDomainResolver`, never for the exact hostname. `noreply@email.openai.com`
  now requests icons from `openai.com` only. Before, a sender could give each recipient a unique
  subdomain and learn from the icon request when and from which IP address the inbox loaded.
- `JmapAccountRepository.connect` finishes a save that has started even when the caller is
  cancelled, including forgetting the old connection and calling `onAccountChanged`. A cancel
  that arrives before the save stores nothing.
- `AccountsState` reloads accounts and calls `onChange` when a connection it was told to cancel
  still saved an account, and leaves `status` at `Idle`. A cancelled account load no longer sets
  `loading` to false while the load that replaced it is still running.
- `AndroidLoginStore` reports Android Keystore `ProviderException` and `IOException` failures
  like other Keystore errors. `secret` returns `LoginSecret.Unreadable` and `save` throws
  `LoginStoreException`, where both used to throw the raw exception. Concurrent first uses no
  longer race to generate the Keystore key.
- `GetMethod`, `QueryMethod`, and `SetMethod` throw `IllegalArgumentException` when `extra`
  names an argument the method writes itself, such as `accountId`, or its `#` result reference.
  Before, `extra` replaced it, so a call could target an account other than the one checked
  against the session. Type-specific arguments such as `position` still work.
- `SwipeActions` runs at most one action at a time. A release cannot commit again until the row
  is back at rest, so dragging a dismissed row that is still shown no longer runs its action a
  second time. A dismissed row that its list has not removed returns to rest after one second.
- `tools/check_architecture.py` rejects `@SuppressLint`, `@SuppressWarnings`, and qualified
  suppressions such as `@kotlin.Suppress` and `@file:kotlin.Suppress`. It checks code inside
  `${...}` string templates, finds literal colors passed to an aliased `Color` import or given as
  decimal numbers, and checks qualified drawable directories such as `drawable-night` and Android
  `res` drawables. Update the apps' copies of the checker to match.
- Add `SummaryCard`, a raised `bgRegular` card with a leading slot beside a one-line title and a
  one-line `text2` subtitle, such as the message a sheet moves.
- `ChoiceSheet` takes an optional `footnote`, shown in `text2` below its options.
- A `SummaryCard` overload takes `people`, a list of the new `NamedAvatar`, and names up to three
  of them on its second line, each after a 20 dp avatar, with "+N" for the rest.
- `PickerSearch` fades and scales its clear button in when text appears and out when it is
  cleared, over 120 ms with the enter and exit curves. It takes a `modifier`, such as one that
  watches its focus.
- `SettingRow` takes a `density`. `RowDensity.Compact` drops the hairline and uses 7 dp vertical
  padding with a 36 dp minimum height, the same as a `SidebarEntry`, and a one-line title. A
  taller trailing control, such as `SidebarExpandButton`, reaches into the row's padding instead
  of making it taller. Other rows keep `RowDensity.Regular` and look as before.
- Add `CollapsibleTree`, which lists items in tree order and lets an item with nested items
  collapse them, with the `Collapsible` motion. Its `row` slot receives whether the item is
  expanded, null when nothing is nested, and a callback that changes it. `expandAll` shows every
  item, such as while a search narrows the list.
- `SelectionRow` is 44 dp tall without a subtitle (`SheetTokens.selectionRowHeight`), with 12 dp
  side padding and no vertical padding, so a leading icon or drag handle no longer makes it
  64 dp. A row with a subtitle keeps its 64 dp minimum with 8 dp above and below the text.
  `ReorderableScope.DragHandle` is 48 dp wide and 36 dp tall. Add `SheetTokens.selectionRowHeight`
  and `SheetTokens.rowPadding`.
- Add `AvatarStack`, up to four overlapping 20 dp avatars outlined in the background color, with an
  optional 14 sp `text2` label and a spoken description. `AvatarStackChip` draws its avatars with
  it.
- `SettingsSection` takes an optional `avatar`, a `ChipAvatar` drawn at 28 dp, 12 dp before the
  title.
- Add `Collapsible`, which unfolds its content downward with a fade while expanded and folds it
  back up, at the default motion speed, for rows nested under a collapsible row.

## 0.7.0 - 2026-10-11

- `AccountSetupScreen` opens its connection form from a "Connect an account" button in the
  shared `SheetFrame`, now public. The sheet scrolls above the keyboard, closes after a successful
  connection, and clears its draft when dismissed. `AccountsState.cancelConnect()` cancels a pending
  connection and drops host-consent credentials; `loading` reports account discovery.
- `ConnectedAccount.emailAddresses` contains the distinct sending addresses returned by
  `Identity/get` for the selected mail account when both the server and that account advertise
  submission. Connected account rows show these beside the sign-in address and server, without
  repeating addresses. Discovery failures keep the saved account visible. Receiving aliases
  that the server does not expose as identities are not discovered.

- `SelectionSheet` now uses `SheetFrame`, the bottom sheet behind `ChoiceSheet`, instead of a
  floating popup. It slides up from the bottom edge with a grab handle and closes when swiped
  down. Clear moves to the header's start and Done to its end; Done applies the draft once the
  sheet has slid away, replacing the Apply button, and `applyEnabled` now enables Done. The close
  icon is gone, and the scrim's accessible name is "Dismiss <title>". The function signature is
  unchanged.
- `SheetFrame` takes `onDone`, `doneEnabled`, and a `leadingAction` that can finish the sheet the
  same way Done does. The first close action wins, so repeated taps during the exit animation
  finish only once. Closing or dragging interrupts the entry animation. Calls that passed
  `content` positionally must name it or use a trailing lambda.
- `ChoiceSheet` and `SelectionSheet` now close when swiped down from any part of the sheet. Before,
  only the header and scrolling content responded, so a swipe that started on a search field, a
  label, or empty space below a short list did nothing.
- `SearchPrompt` no longer shows press or focus feedback. The highlight covered the whole panel
  header, so tapping the prompt changed the field's background. Other controls keep
  `controlPressFeedback`.
- `Chip` and `AvatarStackChip` take an optional `detail` text, shown after a thin divider line
  that is shorter than the text. `PanelTokens.chipDividerHeight` sets its height. The line is
  `accentLight` on a selected chip and `bgLight` otherwise. Mail uses it for the inboxes beside
  the accounts. Named-argument and trailing-lambda calls compile unchanged. `Chip` calls that
  passed `leading` as the sixth positional argument must use `leading =`.
- A `SelectionRow` with neither subtitle nor leading content is now 48 dp tall, the touch target,
  instead of 64 dp. Rows with a subtitle or an avatar keep the taller height.
- Add `ReorderableColumn`, a column whose rows the user reorders by dragging a handle. Each row
  places `ReorderableScope.DragHandle(label)`, a 48 dp target with the `text2` drag indicator. The
  dragged row follows the finger within the column, the rows it passes step aside, and a haptic
  marks each new place. `onMove(from, to)` reports the move once the row is dropped; the caller
  owns the order. Each handle also offers Move up and Move down to accessibility services.
  Keys must be unique and stable. Changes to the row order cancel an active drag.
- Add `symbol_drag_indicator` (Material Symbols Rounded, Apache 2.0).
- Add `ChipPlacement.None` for a `ContextPanel` without pills. The chip rail collapses, so the
  panel is only as tall as its header, and the trailing action moves to the header's end. The rail
  grows and shrinks over the mode transition while the action fades between its places. Code with
  an exhaustive `when` over `ChipPlacement` needs a branch for the new value.

## 0.6.0 - 2026-10-11

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
