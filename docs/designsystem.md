# Design system

The `designsystem` module (`com.otus.designsystem`, artifact `com.otus:designsystem`) holds the
Otus theme, elements, and components. It moved here from otus-mail in version 0.2.0. Elements and
components take values and callbacks; they own no ViewModels, navigation, networking, or storage.
`HtmlView` is the one exception: with `loadRemote`, its web view fetches the images, styles, and
fonts the document names.
The user picks one of four themes and an accent color. Changing any single color saves a custom
theme. The Otus theme with Otus blue is the palette recorded in [mail design foundations](suite/mail-design.md). See
[decision 0014](decisions/0014-user-themes.md).

## Contents

- `theme`: `OtusColors`, `OtusTheme`, `OtusPalette`, `ThemeBase`, `ThemeChoice`, `ThemeColor`, `CustomTheme`, `AccentPreset`,
  `accentShades` (with `AccentShades`), color helpers (`hslColor`, `toHsl`, `contrast`, `toHex`,
  `parseHexColor`), `FolderColor`, `ThemeTokens`, `Motion` (with `MotionSpeed`, `MotionCurve`, and
  `LocalMotionScale`), the corner scale (`CornerTokens`), and tokens for avatars, calendars, control
  feedback, pages, panels, refresh, sheets, the sidebar, the action bar (`ActionBarTokens`), and
  compose fields (`ComposeTokens`), reply threads (`ThreadTokens`), and rule chips and folder
  settings rows (`RuleChipTokens`, `FolderRowTokens`), the filter panel (`FilterTokens`), and
  shared opacities (`OtusAlpha`).
  `panelTextStyle()` uses Figtree.
- `elements`: `ActionButton`, `ActionRow`, `AnimatedText`, `Avatar`, `AvatarImage`, `BarIconButton`, `BarAddButton`, `CalendarDay`,
  `AddressPill`, `ButtonPill`, `ComposeField`, `HtmlView`, `QuoteChip`, `RichTextBody` (with `RichText` and `TextFormat`),
  `CalendarMonthBackground`, `Chip`, `ColorSlider`, `ColorSwatch`, `AddSwatch`, `SwatchStack`, `IconButton`, `IconPill`, `InputField`, `LabelPill`, `RefreshIndicator`,
  `FolderColorSwatch`, `LocalIconTint`, `RowButton`, `SearchField`, `SearchPrompt`, `SelectionRow`, `SidebarEntry`, `SidebarHeading`,
  `SidebarCount`, `SidebarExpandButton`, `SidebarPill`, `SidebarInboxEntry`, `SidebarMailboxTile`,
  `SidebarFolderEntry` (with `SidebarFolder`), `SidebarDivider`, `SectionCaption`, `Symbol`, `Toast` (with `ToastAction`),
  `ToggleSwitch`, `ValueButton`, `FilterChip` (with `FilterChipRole` and `ChipLabel`), `FolderGlyph`, `BadgedSymbol`, and the
  `controlPressFeedback` modifier.
- `components`: `ActionBar`, `ActionBarHost`, `BarAction`, `BarContext`, `BarToken`, `ActionDialog`, `ChoiceDialog`, `ChoiceSheet` (with `ChoiceRow`), `ConfirmDialog`, `InputDialog`, `InputSheet`, `ContextPanel`, `DatePickerDialog`,
  `MonthNavigation`, `MonthYearPicker`, `PageScaffold`, `Subpage`, `PageSection`, `PanelSurface`,
  `PickerSearch`, `PreviewCard`, `PullToRefresh`, `ReorderableColumn`, `ReorderableTree`, `SelectionSheet`, `SheetFrame`, `SettingRow`, `SettingPageRow`,
  `SettingToggleRow`, `SettingsSection`, `SpecificDatePicker`, `Sidebar`, `SidebarHeader`, `SidebarMailboxGrid`, `ScopeSheet`, `ScopeRow` and `ScopeChildRow` (with `ScopeOption`), `PickerChip`, `AccountGroupRow`,
  `RuleChip`, `RuleChipGroup`, and `AddRuleChip` (with `RuleChipLine`, `RuleChipValue`, `RuleSegment`, and
  `RuleSegmentStyle`), `FolderSettingRow` with `FolderRuleSummary` and `FolderOwnerLine`, `SummaryCard` and `PersonText` (with `NamedAvatar`), `ValueToken`, `ValuePicker`, `Collapsible`, `CollapsibleTree`, `AvatarStack`, `ContextMenu` and `QuickActionBar` (with `MenuAction`),
  `Dropdown` (with `DropdownRow`), `OptionsMenu` (with `OptionsItem`), `FormattingBar` (with
  `FormatButton` and `CompactSendButton`), `BarMorph`, `AttachmentChip`, `RecipientLine` (with `Recipient`, `AddressInput`, and `RecipientChip`), `ThreadStrip` (with `ThreadEntry` and `ThreadMessageRow`),
  `PersonAvatar`, `BarNotice`, `ClosableSheet` (with `SheetConfirm`), `SegmentedToggle`, `PickRow` (with `PickMark`), `PersonRow`, `RowGroup` (with `GroupRow`), `TimeWheelPicker`,
  `ResizableImage` (with `ImageSizing` and `ImagePreset`), `ListCaption`,
  `FilterPanel`, `FilterPanelHeader`, `FilterPanelFooter`, `FilterSection`, `FilterFolderRow` (with `FolderCheck` and `TreeFolder`),
  `SidebarSwipeArea`, `SlidingOverlay`, `SlidingPages`, `SwipeActions`, `ThemeCard`, and `ToastHost`
  (with `ToastState` and `ToastMessage`).
- Settings rows follow Notion's settings pages: flat rows with a title and `text2` description
  beside the control, `bgLight` hairlines between them, and large section titles. The whole row is
  the control; `ToggleSwitch` and `ValueButton` only show its state. A long picker, such as Mail's
  folder lists, uses `RowDensity.Compact`: 36 dp per row and no hairlines. Folder lists, including
  `SidebarEntry` and `SidebarFolderEntry` rows, use `PanelTokens.compactRowHeight`, below the
  48 dp touch target by the user's choice. Nested rows indent with `Modifier.nestIndent`, which
  stops after four steps (`MAX_INDENT_STEPS`), so a row at the fifth level or deeper keeps room
  for its name. A row without a click is
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
  is a 52 dp row for the main mailbox, inset to line up with the grid or, with `inset = false`,
  as wide as `SidebarEntry` rows. `SidebarMailboxGrid` puts `SidebarMailboxTile`s in three
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
- `ToastHost` shows short messages from a `ToastState` in a pill as low on the screen as it can
  go. Each `ActionBarHost` with a bar reports its height to `LocalBottomClearance`, and the toast
  sits one bar inset above the highest bar on screen, or one inset above the navigation bar and
  keyboard on a page without one, such as Settings. It moves between the two at the normal speed.
  A toast rises in from below on the enter curve. When its time is up or its action runs, it
  sinks, shrinks to 90 %, and fades on the exit curve, 240 ms each. Over a bar it is clipped at
  the bar's top edge, so it passes behind the bar. A
  `ToastMessage` may carry a `ToastAction`, such as Undo, with a ring that counts down its time:
  6 seconds with an action, 4 without, longer when a screen reader is on. A message with the same
  `group` as one on screen replaces it and restarts its timer, so the app can count up to
  "3 emails deleted"; `isShowing(group)` tells the app whether to keep counting. Tapping the
  action runs it and closes the toast. A `sticky` message has no timer and stays until its action
  runs or it is swiped away, such as a failed send with Retry. `onClose` runs when a toast leaves
  without its action, so the app can finish what Undo would have reverted. `durationMillis`
  replaces the default time, such as an Undo window the user chose. Other messages stack with the newest in front and up to two older ones
  peeking out behind it, each on its own timer. Tapping the stack fans it out and pauses every
  timer; tapping again or system back stacks it again. The group is as wide as its widest toast.
  A toast can be dragged sideways in either direction: it lifts a shade lighter, its timer
  pauses, and a darker `bgDark` end opens on the side it left. The end stretches outward, away
  from the drag, by a quarter of the drag up to 16 dp, and always reaches under the toast's round
  end, so the two never part; only its outer end is round. Its close symbol and "Dismiss" sit at its
  outer edge and are always drawn, so the toast uncovers as much of the label as the drag allows.
  Past a third of its width both brighten and a haptic marks it. Releasing there slides the
  toast off the screen on the exit curve while the end fades; the host spans the window and fades
  anything within a bar inset of its sides, so the toast blurs out at the screen's edge. The
  swipe removes the toast without running its action; shorter drags spring back. Colors are `bgDark` at rest and `bgRegular` lifted, changing over 240 ms, with `strokeBgDark`
  outlines.
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
  not click the row or scroll the list. The dragged row gets a `bgLight` fill, and in a
  `ReorderableColumn` the other rows step aside into the places they would take.
- `ReorderableTree` rearranges a short tree, such as folders, flattened into rows that each know
  their depth. Its rows use the same `ReorderableScope` as `ReorderableColumn`. The other rows stay
  in place while the dragged row and its nested rows follow the finger with their `bgLight` fill
  at 75% opacity, so the row under them and the line show through. The nested rows are never
  targets, so a parent always shows a line once it leaves its own spot. The tree judges the target by the dragged row's center. Over the middle half of a row, the folder goes inside that
  row as its first child. Over the upper or lower quarter, it goes into the gap above or below.
  The dragged row can reach half its height past either end, so the first and last gaps are
  reachable. A 2 dp `accentLight` line with round ends and a small ring at its start marks the
  target. For a gap, the line sits in the gap at the depth of the row below it, or of the last row
  at the end. For a drop inside a row, it sits under that row one level deeper. The line starts
  where a `FolderSettingRow` at that depth starts its icon, steps 20 dp per level, and stops at
  four steps like `nestIndent`. It is drawn over the rows and does not move them. The dragged row,
  and the gaps on either side of it, show no line and drop nothing. The app's `canDrop` hides the
  line for targets it would refuse, and a haptic marks each new target. On release the app gets
  `onDrop(from, target)` with a `TreeDropTarget`: `Before(index)` for the gap above that row, where
  an index equal to the row count means after the last row, or `Into(index)`. Move up and Move
  down drop a row into the gap above the previous row or below the next one, and only appear when
  `canDrop` allows that.
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
- `TrailingIcons` puts up to two icons at a row's end, each centered in a 48 dp slot with no gap,
  so rows that use it, such as `FolderSettingRow` and `AccountGroupRow`, line them up.
- `InputSheet` asks for one line of text, such as a new name, in a `SheetFrame`: the field is
  focused when it opens, the header button confirms, and `problem` disables it while the text
  can't be used. `InputDialog` asks the same in a popup.
- `QuickActionBar` is a small pill of icon buttons that opens just above where the user
  long-pressed, centered on the point and kept inside the screen, or below the point when there
  is no room above. It takes the same `MenuAction`s as `ContextMenu`, each with an `icon`; screen
  readers hear the label. It sits on the menus' `bgDark` surface with a `bgLight` outline and
  opens with the same fade and grow. Choosing an action closes it first.
- `FolderSettingRow` is one folder on a folder settings page: the folder icon, the name in 17 sp,
  an optional detail line, a `text2` settings icon, since tapping opens the folder's settings, and
  an optional `handle` after it, at least 56 dp tall and indented 20 dp per level. A 1 dp
  `bgLight` guide runs down the row under the icon of each level above it, so consecutive rows
  draw a tree. A long press calls `onLongClick` with the pressed point, for a `QuickActionBar`.
  Inside a `ReorderableTree` or `ReorderableColumn`, the `ReorderableScope.FolderSettingRow`
  overload adds the row's drag handle after the settings icon. The
  icon expands and collapses subfolders like `SidebarFolderEntry`'s, shares its code, and takes
  `LocalIconTint`. `SidebarFolder.icon` replaces the closed folder, such as `symbol_folder_shared`. The detail
  is a slot: `FolderRuleSummary` shows the first rule as a compact chip and a `text3` count such as
  "+1", and `FolderOwnerLine` shows the owner's 16 dp avatar before text such as "Shared by
  Florian · can read". `PickerChip` is a pill with the current choice's avatars,
  its label, and a down chevron, such as which account a page shows; it opens the app's picker
  and reads as one button. `Subpage` and `PageScaffold` take `chips` for the header's chip row and
  then lay the panel out like search, with the pills overlapping its bottom edge.
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
  scale in, 120 ms on the exit and enter curves; the shell stays. With `minimized`, a `BarMinimized`, the bar shows only the primary's icon in a
  62 dp circle at its end, as far from the screen's end edge as from its bottom edge (the
  navigation bar counts toward both), filled like the full primary. Its ring is `accentLight`. With a `BarContext`, a fully rounded
  `bgDark` shell with a `strokeBgDark` outline grows out from behind the circle toward the start,
  240 ms on the enter curve, and shrinks back behind it on the exit curve. The context follows the
  frontmost bar like the icon, so it grows when a selection starts and shrinks when it ends. The
  whole button runs the primary. It stays in place while the
  keyboard moves. With `BarMinimized.hidden`, or when the keyboard starts opening, it fades and scales out like
  `PickerSearch`'s clear button, 120 ms on the exit curve. It fades and scales back in on the
  enter curve once the closing keyboard drops below it. Pass `hidden` as soon as a field will take
  the keyboard, because the keyboard otherwise covers the button within a few frames. When its primary changes, including when a page opens over another or closes
  back to it, or when a selection's bar replaces a list's, the old icon fades and scales out while the new one fades and scales in inside
  the circle, which stays, at the same speed and curves and read as "label, description"; the
  secondary actions are not drawn. With `BarMinimized.more`, a 62 dp `bgDark` circle with a `strokeBgDark` ring
  and a ⋯ icon sits at the bar's start, balanced against the navigation bar like the primary. It
  scales in when it first appears and hides with the primary. Tapping it opens the `MenuAction`s
  in a `ContextMenu` 8 dp above it, lined up with its start edge, which leaves the screen behind
  it undimmed. A `BarAction` with a `menu` opens those `MenuAction`s
  in the same `ContextMenu` instead of running its click: 8 dp above a secondary button, lined up
  with its start edge, or above the primary, lined up with its end edge. The menu scrolls once it
  reaches 480 dp. With `onAdd`, a full bar with fewer than three secondary actions ends its group
  in a `BarAddButton`: a 36 dp dashed `text3` circle with a `text2` plus, shaped like
  `AddSwatch`, so an empty slot in settings reads as room for an action and not as one. The app chooses the actions and the context; the component only
  draws them and reports clicks. `ActionBarHost` places the bar over the bottom of a page, above
  the navigation bar and keyboard, and pads the page's content so it can scroll clear of the bar.
  Mail uses it for an open message and for selected messages; see
  [Mail's action bar guide](https://github.com/reazndev/otus-mail/blob/main/docs/action-bar.md).
- The Write page's parts follow Mail's "Write" boards. `ComposeField` is a header line, such as
  Subject: a `text2` label in a 56 dp column beside a borderless field with a `text3` placeholder
  and a `bgLight` hairline below. `RecipientLine` uses the same column for To, Cc, and Bcc: a
  `RecipientChip` for each person, with a 22 dp avatar in an outlined pill, wrapping as
  needed, and a field for the next address. Next or losing focus commits the typed text, and
  Backspace in an empty field removes the last chip. `RichTextBody` edits the formatted body,
  built on the compose-rich-editor library, which stays an implementation detail behind
  `RichText`: it reads and writes HTML and plain text. A text field cannot draw images, so
  `RichText` splits its HTML at each `<img>` into text blocks with an image block between each
  two. `insertImage` splits the text at the cursor, `removeImage` joins it again, and Backspace
  at the start of the text after an image removes it; `RichText.images` (`RichTextImages`)
  inserts, removes, resizes, describes, and replaces images and knows the selected one.
  `RichTextBody` draws each image through its `images` slot as a `ResizableImage`, as wide as
  the text until it is resized to Small (120), Medium (200), Large (280), or any width; the width
  and description go into the `<img>` tag, which also caps it at 100%. Formatting goes to the
  text block that last had focus.
- `ResizableImage` shows an `EditableImage`; selected, it gets an outline and a corner handle.
  Dragging the handle draws a dashed guide at each preset with its short label (S, M, L, Full),
  snaps within 12 dp with a haptic tick, and names the size in a pill under the handle. Its
  other controls live in `ImageBar`: the page's bottom bar while an image is selected, with the
  presets in a `SegmentedToggle`, ⋯, and the accent ✓ in the primary's place.
- `ExpandingBarGroup` is a bar group whose ⋯ grows the group itself upward: any panel, such as
  an `OptionsList`, above a divider and the group's row, with ⋯ filled while open. ⋯, Back, or
  the panel's close folds it. `OptionsList` is the rows of an `OptionsMenu` for any surface. `BarMorph` is generic over which bar shows, so a page morphs between its action bar,
  formatting row, and image bar. `RichText.selectedImageSizing()` gives the bar the sizes the
  body uses. `rememberImage` decodes a photo off the main thread at
  most 2048 px on a side. `QuoteChip` stands for the collapsed
  original at the end of a forward. With `collapsedAfter`, a `RecipientLine` holding more people
  shows that many and a "+N more" chip, without its field, until the chip is tapped; its label
  then stays beside the first row (`ComposeLine` with `topAligned`).
- `ThreadStrip` shows a reply's thread above the action bar in a `bgDark` panel. Up to three
  `ThreadEntry` items show as 40 dp rows with a 22 dp avatar, the name, and the date, divided by
  hairlines. More than three fold into one 56 dp row: an `AvatarStack`, the caller's summary and
  `text3` detail, and an unfold symbol. The whole strip is one button. `ThreadMessageRow` is a
  message in the thread's sheet: a 32 dp avatar, name, date, and a one-line preview.
- `BarToken.People` takes an optional second avatar, drawn overlapping the first by
  `ActionBarTokens.pairOverlap` with a ring, for mail to several people.
- `FormattingBar` docks above the keyboard: a `bgDark` 56 dp row with an optional leading button,
  such as Attach, behind a divider, then Bold, Italic, Bulleted list, Numbered list, Link, and
  Clear formatting. The buttons scroll sideways with faded edges, and a trailing slot, such as
  `CompactSendButton`, stays pinned. An active format gets a `bgLight` fill, never the accent.
  Over the keyboard the row reaches the page gap at both sides. `CompactSendButton` has the
  action bar's collapsed action shape and sits flush at the row's end, its accent border over
  the row's.
- `BarMorph` turns a `FormattingBar` into a full `ActionBar` and back: each part takes the other's
  height, then its width, so the row splits into the group and shell. The content blurs while the
  width changes. Pair it with `isKeyboardUp()`, which turns false as the keyboard starts to close.
- `AttachmentChip` is a file on a message being written: an attach symbol, the name with a
  `text3` detail under it, such as the size, and a ✕. It is outlined in `bgLight`, at least 48 dp
  tall and at most 280 dp wide. A failed upload can make the chip retry on a tap.
- `Dropdown` opens a `bgDark` list with a 30 dp radius under the element it is called from, the
  window's width unless given one, and above the element when there is no room below.
  `DropdownRow` marks the selected row with a `bgRegular` fill and no checkmark; `ListCaption`
  heads a group in `text3`. A non-focusable dropdown leaves the keyboard to a field, for
  suggestions while typing. `OptionsMenu` is the 286 dp menu under a ⋯ button: icon rows that
  act, open more with a chevron, or switch an option, with dividers between groups.
- `ClosableSheet` is a sheet titled at its start with a ✕ at its end and an optional full-width
  `SheetConfirm` button at the bottom; `SheetFrame` shares its surface. `SegmentedToggle` picks one
  of a few options with a `bgLight` fill. `RowGroup` puts `GroupRow`s, with an icon, a label, and a value or a chevron, on one
  `bgRegular` card. `TimeWheelPicker` picks a time like the date chooser. `PickRow` is a sheet choice with a `text1` radio or
  checkbox, never the accent, and dims without an action. `PersonRow` opens an address's page,
  with an avatar, a `text2` or muted `text3` line, and a chevron. `ResizableImage` shows a
  signature image with a selection outline, a ✕, a corner handle that snaps to `ImagePreset`s, and
  a size pill under it. `ListCaption` heads a group of rows in `text3`.
- `ButtonPill` is a pill that acts, such as "+ Cc", and `AddressPill` shows the address a message
  is sent from, avatar first and a chevron last, with long addresses ending in an ellipsis.
- `BarNotice` sits just above a page's action bar and says what its main action will do
  differently, such as "Scheduled for Mon 19 Oct, 8:00", with a button such as Change.
- `FilterPanel` grows down out of a control panel, on its surface, border, and radius, over a
  scrim, so it reads as that panel opening. Its header and footer stay put while the body scrolls,
  and it is at most as tall as the room its modifier gives it. Back, a tap on the scrim, and an
  upward swipe close it; screen readers read only the panel while it is open and start at its
  title. `FilterPanelHeader` has the title, or a back button named after the page it returns to,
  and a plain ✕ at the top right. `FilterSection` puts a small `text3` label above wrapping
  `FilterChip`s, which are `bgRegular` capsules while off and `accentDark` while on, read as a
  checkbox, a radio, or a button. A `ChipLabel`'s prefix, such as a folder's parents, shows in `text3`.
  `FilterPanelFooter` has a Reset pill, dimmed with `OtusAlpha.DIMMED` while there is nothing to
  reset, and the primary button, whose label screen readers announce politely when it changes.
  `FilterFolderRow` is one folder of a checkable tree: a chevron for parents, a `FolderGlyph`, the
  name after an optional `text3` path, and a checkbox, dimmed while a parent includes it.
  `FolderGlyph` is a filled folder in its `FolderColor`, or an outlined symbol in `text3`.
  `BadgedSymbol` puts a small `accentLight` count on a symbol, such as the active filters on the
  filter button.
- `BarContext.lead` puts a small line in place of the primary's label, so Send reads "Send to"
  above "Grace".

All visual tokens live here, including `InboxTokens`. New elements and reusable components
belong here even when only Mail currently uses them. Their APIs use values and callbacks,
without app dependencies.

## Corners

Every radius comes from one scale in `theme/CornerTokens.kt`. Feature tokens such as
`SheetTokens.radius` or `ComposeTokens.recipientRadius` name a step, so a change to the scale
reaches every part that uses it. A part inside another takes the outer radius minus its inset:
a menu row is `MenuTokens.radius - MenuTokens.inset`, and a theme sketch is
`ThemeTokens.cardRadius - ThemeTokens.cardPadding`. Pills, avatars, and circles round to half
their height and take no step. No corner rounds past half its own height. `RoundedCornerShape`
scales its corners down for that, and code that draws its own rounded rectangles caps the radius
itself.

| Step | Radius | Used by |
| --- | --- | --- |
| `tiny` | 6 dp | Ends of a calendar date range |
| `small` | 10 dp | Compact rule chips, theme sketch bars |
| `chip` | 14 dp | Quote and attachment chips, value buttons, rule chips, date tokens, the calendar month outline, page sketches, swipe reveal corners |
| `button` | 20 dp | Formatting buttons, search fields in the filter panel, theme cards |
| `panel` | 22 dp | The control panel at the top of a page and the `FilterPanel` that grows out of it |
| `row` | 24 dp | Rows and cards in pages, menus, and sheets, row groups, sidebar entries, the signature editor, custom theme previews |
| `large` | 28 dp | The formatting row above the keyboard and its Send, nudge cards |
| `surface` | 30 dp | The action bar, menus, dropdowns, and the top corners of sheets, dialogs, and wheel pickers |

Only the control panel keeps 22 dp. `InboxTokens.selectionRadius` follows it, so a selected
message row rounds like the panel. A `BarMorph` clips each part to the smaller of the formatting
row's and the action bar's radius, so neither bar loses an edge while one turns into the other.

## Motion

Every animation in the design system and in Mail takes its duration and curve from `Motion`, in
`theme/Motion.kt`. The rules for picking a speed and a curve are in
[mail design foundations](suite/mail-design.md#motion).

`MotionCurve` holds five cubic Bézier curves:

| Curve | Bézier | Use |
| --- | --- | --- |
| `Standard` | (0.4, 0, 0.2, 1) | The default, for anything that changes in place |
| `Reverse` | (0.8, 0, 0.6, 1) | `Standard` played backward, for undoing a change so it retraces its steps |
| `Enter` | (0.2, 0, 0, 1) | Decelerates, for sheets, the sidebar, and text that arrive |
| `Exit` | (0.4, 0, 1, 1) | Accelerates, for the same things as they leave |
| `Rise` | (0.2, 0.8, 0.25, 1) | Jumps off at once and settles slowly, for the launch animation only |

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

`SlidingPages` and `SlidingOverlay` slide whole pages, but chrome that both pages draw stays
still. When the page that leaves and the page that arrives both have a `ContextPanel`, or both
have a bar in an `ActionBarHost`, that chrome keeps its place while the rest of each page slides
under it. The arriving page's chrome draws on top, and the leaving page's copy fades out under it.
Chrome that only one of the pages has slides with its page.

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

## Brand

`designsystem/brand/` keeps the source SVGs: the Otus logo, the owl on its own, the Mail app icon,
and Mail's launch animation in a play-once and a looping version. `tools/brand_drawables.py`
converts them into drawables. Change the SVG, or an `AccentPreset` color, and run it again.

- `brand_otus_logo` and `brand_owl` are Compose drawables of the logo and the owl. Nothing uses
  them yet. `symbol_owl` is a one-color owl for symbol slots, such as the App icon setting.
- The launch animation's layers each hold one color, so `MailLaunch` tints them from the theme.
  They share a 128 unit canvas so they line up when stacked.
- `otus_mail_icon_<name>` are Mail's launcher icons in `androidMain/res`, which merge into the
  app: `monochrome` and one per `AccentPreset`, such as `otus_mail_icon_otus_blue`. Each is
  adaptive from Android 8 on, with the owl and envelope scaled into the 66 dp safe circle, and
  the full square icon before that. A launcher icon cannot follow the theme, so all of them sit on
  `otus_brand_background`, the Otus theme's `bgDark`. A host test checks every preset has one.

`AppIconStyle` picks how the owl is colored: `Accent` uses `accentRegular` and `Monochrome` uses
`text1`. `AccentPreset.closest` picks the icon for a custom accent.

`MailLaunch` plays the launch animation in the current theme: the owl and envelope in the style's
color, and the background and dark details in `bgDark`. It starts with the play-once version.
While the app reports `loading`, it continues with the loop, starting from the loop's settled
pose, which matches where the intro ends. Once the owl has arrived and loading has finished, or on
a tap, the mark shrinks to `BrandTokens.LAUNCH_EXIT_SCALE` and the whole screen fades out at
`Relaxed` speed on the `Exit` curve. `MailLaunchTimeline` holds both runs' keyframes as fractions
of `Motion.LAUNCH_MILLIS` (3 s) and `Motion.LAUNCH_LOOP_MILLIS` (4.2 s), and `KeyframeTrack` eases
between them like a CSS keyframes rule. The tilt and the blink use `Standard` where the SVGs use
ease-in-out and linear timing. `LocalMotionScale` scales all of it.

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

Common tests cover the continuous calendar week model, decoded-avatar cache, motion durations,
reorder behavior, theme choices, and sheet animation interruption and completion. Android host
tests cover image decoding, `ValuePicker` behavior, and the screenshots below. Some Compose UI
tests for panels, pickers, motion, and press feedback still run in Mail's `androidApp`.

### Screenshot tests

Each element and component has Roborazzi screenshot tests in `src/androidHostTest`. A test
renders the composable with Robolectric's native graphics at 360 × 640 dp and xhdpi, and
compares the image with a PNG in `src/androidHostTest/screenshots/`. Any changed pixel fails the
test, and Roborazzi writes the comparison image to `build/outputs/roborazzi/`.

Write a test with the `screenshot` helper in `screenshot/Screenshot.kt`:

```kotlin
@Test
fun selected() = compose.screenshot("chip_selected") { Chip("Work", {}, selected = true) }
```

The helper draws the content on `bgDark` in `OtusTheme`; pass a palette to draw it in another
theme. Use fixed dates and demo data, so an image changes only when the drawing does.

After an intended visual change, record the new images, look at each changed PNG, and commit
them with the change:

```sh
./gradlew :designsystem:testAndroidHostTest -Proborazzi.record
```

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
