# Changelog

## Unreleased

- JMAP: add `downloadStream(accountId, blobId, mediaType, name, receive)` to `JmapConnection`, and
  the same with a session and credentials to `JmapClient`. It reads a blob through the session's
  `downloadUrl` with the checks of `download`, but hands each piece of up to 64 KiB to `receive` as
  it arrives instead of collecting the body, so a blob larger than `maxResponseBytes` can go
  straight to disk. It returns the media type the server sent. An exception from `receive`, or
  cancelling the caller, ends the transfer.
- `ActionBar` takes `allActions`: a drag up on the secondary group grows it into a menu of them,
  following the finger. A secondary action's `menu` now opens the same way, as one surface with
  a panel above the group, instead of in a `ContextMenu` above its button. The panel keeps the
  bar's gap around the primary's corner, and the group's own icons hide while it is open.
- Add `Motion.paced(distance, curve, scale)` and `Motion.PACE_DP_PER_MILLI`, a tween whose length
  follows the distance it covers, between Quick and Relaxed, so short and tall surfaces grow at
  the same speed.
- Accounts: an account without a sending address no longer crashes Connected accounts. Its avatar
  shows without a button, and `AvatarButton` takes a null `onClick` for an avatar that does
  nothing. Callers compiled against the earlier signature must rebuild.

- Add `DependentSettings(enabled)`, for setting rows that only apply while another setting is
  set. Unless enabled, every `SettingRow`, `SettingToggleRow`, and `SettingPageRow` inside stays in
  place, draws its title, icon, value, and switch dimmed, and ignores taps; its description says
  what it waits on. `LocalSettingsEnabled` carries the state, and `ToggleSwitch` takes `enabled`.
- Settings search: `setting` and `toggle` take `keywords`, other words for the setting, which
  search matches as it matches options, and a page names its own with `keywords(...)` in its
  content. Items inside `unavailable(reason) { ... }` carry the reason their page shows them
  disabled, and their results show it in place of their section. `SettingsItem` exposes
  `keywords` and `unavailable`. A query of several words now also finds an item that holds each
  word in its title, options, keywords, or path, ranked after whole matches. `appearanceIndex`
  adds keywords such as "Dark mode" for Theme. Callers compiled against the earlier signatures
  must rebuild.
- Add `LazyListState.animateScrollToItem(index, animationSpec)` in `theme.motion`, which scrolls to an
  item on a Motion tween where Compose's own `animateScrollToItem` always uses a spring that
  ignores the user's animation speed. `WheelPicker` and the date range calendar now scroll with it,
  and `tools/check_architecture.py` rejects an `animateScrollToItem`, `animateScrollToPage`,
  `animateScrollBy`, or `animate*AsState` call left on its default spec. Update the apps' copies of
  the checker to match.
- Accounts: `parseJmapServer` reads a server address longer than a DNS name and port, 259
  characters, as `Invalid` before matching it, so pasting thousands of host labels no longer
  overflows the stack.
- JMAP: add `requireTrustedBlobOrigins` to `JmapClient.session`, false by default. When true,
  discovery also throws `RedirectedElsewhere` for a `downloadUrl` or `uploadUrl` on an origin the
  user has not approved, so a sign-in can ask before every upload and download would fail.
  Without it, discovery is unchanged: such a session still serves API calls, and only its blob
  calls throw `RedirectedElsewhere`. Callers compiled against the earlier signature must rebuild.
- Accounts: connecting asks for approval of a blob host on another origin, as it does for a
  redirect or an `apiUrl` elsewhere, and saves it with the login's trusted origins. Saved logins
  keep loading; adding such an account again approves its blob host.
- JMAP: add `JmapPolicy.socketTimeoutMillis`, 30 s by default, the longest any request may go
  without data. Blob uploads and downloads no longer have `requestTimeoutMillis` as a total limit,
  so large attachments work on slow links. Callers that build `JmapPolicy` must rebuild.
- JMAP: add `JmapException.BlobNotFound`, which a download now throws for HTTP 404 instead of
  `NotJmap`. `JmapConnection` keeps its cached session after `BlobNotFound` and
  `RedirectedElsewhere`. Breaking: an exhaustive `when` over `JmapException` needs a branch for it,
  and code that caught `NotJmap` for a missing blob catches `BlobNotFound` instead.
- JMAP: `JmapException.RequestFailed` takes and exposes `limit`, the limit a
  `urn:ietf:params:jmap:error:limit` problem names (RFC 8620, section 3.6.1). Code that constructs
  it must rebuild.
- `RichText` finds a draft's `<img>` tags with HtmlView's linear scan instead of a regular
  expression, so markup with many `<img` and no `>` no longer stalls the editor. Only real
  `<img>` tags with a `src` split the text: one inside a comment or another tag no longer does, and
  a `src` or `width` in single quotes or none now counts.
- Add `InputKind.Username`, a login name on the email keyboard. It and `InputKind.Password` now
  tell password managers and other autofill services which login field they are, and
  `AccountSetupScreen` uses them, so a password manager can fill the account form. An exhaustive
  `when` over `InputKind` needs a branch for `Username`.
- `TimeWheelPicker` no longer crashes when `allowed` holds no five-minute step, such as 10:01 to
  10:04; it offers the range's start alone.
- `BarMorph`'s fades follow `MotionCurve.Standard` through `Motion` instead of Compose's default
  easing, which draws the same curve, so a change to the curve now reaches them.
  `tools/check_architecture.py` rejects `spring`, `CubicBezierEasing`, and a `tween` or
  `TweenSpec` without an easing or with a literal duration outside `theme/motion`. Update the
  apps' copies of the checker to match.
- `decodeImage`, `rememberImage`, and `rememberAvatarImage` decode images with a side over
  4096 px, such as 24-megapixel photos and panoramas, instead of returning null. They still sample
  the image down to `maxSize` while decoding; only an image of more than about 268 million pixels
  is refused.
- `HtmlView` builds its page on a background thread and shows nothing in its place until the
  first page is ready. It reads the message's markup in one linear scan instead of regular
  expressions, so markup without a closing `>` or a long unquoted `data:` image source no longer
  stalls the main thread or overflows the stack. Only real tags set its colors, layout, and image
  placeholders: an `<img>` or a `color` attribute inside a comment, a style sheet, or an attribute
  value no longer counts, and `<img/src=…>` now gets a placeholder. In a plain-text quote, a
  Markdown link's label no longer contains `[`.

- `PushSubscriptions` saves the data types of each login's subscription, and `sync` replaces a
  subscription whose saved types differ from the `types` it was given, because a server keeps the
  types a subscription was created with. Records saved by earlier versions have no types, so each
  login's subscription is created again once after the update and goes through verification
  again. The public API is unchanged.
- Add `SmoothCornerShape`, a `CornerBasedShape` whose corners ease into their edges like Figma's
  corner smoothing at 0.6 (G2 corners), and `Modifier.strokeBorder`, a border that strokes a
  shape's path directly so it stays on the fill at fractional positions. Every rounded surface in
  the design system and the settings screens now draws with `SmoothCornerShape` and its borders with
  `strokeBorder`; a corner near half the shorter side stays a plain arc, so pills keep round ends.
  An app that draws `RoundedCornerShape` from a token next to Meta's surfaces gets plain corners.
- Corner tokens round about a quarter less. `SheetTokens.radius` and `MenuTokens.radius` go from
  30 to 22 dp. `SheetTokens.rowRadius`, `SignatureTokens.editorRadius`, `ReadTokens.bannerRadius`,
  and `ThemeTokens.customPreviewRadius` go from 24 to 18 dp. `PanelTokens.panelRadius` and
  `FilterTokens.cardRadius` go from 22 to 16 dp. `ThemeTokens.cardRadius` and
  `FilterTokens.searchRadius` go from 20 to 15 dp. `NudgeTokens.radius` goes from 28 to 20 dp.
  `ActionBarTokens.dateRadius`, `ComposeTokens.recipientRadius`, `ComposeTokens.quoteRadius`,
  `ReadTokens.placeholderRadius`, and `CalendarTokens.monthCornerRadius` go from 14 to 10 dp.
  `ThemeTokens.sketchBarRadius` goes from 10 to 8 dp, and `CalendarTokens.endpointRadius` from 6
  to 4 dp. `ActionBarTokens.radius` is now half of `collapsedActionWidth` and
  `ComposeTokens.formatButtonRadius` half of `formatButtonSize`; both keep their values, 30 and
  20 dp.
- Add `AppIconStyle.Chrome`, `Rainbow`, `Trans`, `Bi`, and `Lesbian`, with their Android launcher
  icons (`otus_mail_icon_chrome` and so on) and Compose drawables (`brand_mail_icon_*`). An
  exhaustive `when` over `AppIconStyle` needs a branch for each, and an app that offers the styles
  as launcher aliases needs an alias for each. `AppIconStyle.markColor` is now `text1` for
  `Monochrome` and `Chrome` and the accent for every other style; before, every style but `Accent`
  was `text1`.
- Add `MailAppIcon`, a launcher icon preview in its rounded tile, `IconChoice`, a picture option in
  a grid that is one radio button, `BrandColors` with the launcher icons' tile colors, and
  `BrandTokens.appIconSize`, `appIconRadius`, and `ADAPTIVE_ICON_BLEED`. The settings App icon
  row opens a grid of the icons instead of a list of names.
- Add `otus_mail_status_icon`, a white owl and envelope drawable for Android's status bar.
- Add `ToastMessage.swipeAction`. When a toast is swiped away, it runs instead of `onClose`, and
  its label replaces Dismiss as the screen reader's swipe action, such as Send for a held message.
  Without it, a swipe still runs `onClose`. `ToastMessage` is a data class, so code that
  destructures it or calls `copy` positionally must be recompiled.
- Breaking: rebuild `ContextPanel` after the Otus v2 control panel design. It runs edge to edge,
  draws behind the status bar and pads its content below it, and rounds only its 30 dp bottom
  corners, with a stroke on the bottom edge, and has one 56 dp header row. Pills sit across the
  bottom edge in a 4 dp page-colored ring. `compact: Boolean` is
  replaced by `collapse`, a 0 to 1 scroll progress from `rememberPanelCollapse(list)`, whose
  `connection` follows a list's scroll: the panel shortens over 60 dp and its pills rise, shrink,
  and fade out. Releasing halfway settles on the nearer state, and the list's top expands it.
- Add `PanelNavigation`, the header's chevron or ✕ with its glyph at the start of the 48 dp
  target, and `PanelTitle`, a 20 sp semibold page title.
- Add `PanelSearchPill`, the inbox's "Search Inbox" pill, which tightens from 52 to 44 dp as the
  panel collapses and grows a filter button in at its end. Add `AccountStack`, overlapping
  account avatars that fold into one with a "+N" badge, and `PanelSearchField` and
  `SearchHitRow` for a search in the panel.
- Add `PanelSheet` and `PanelSheetHeader`: the control panel unfolded for a task over a scrim,
  with a grab handle. `FilterPanel` is built on it.
- Breaking: `FilterPanel` has no ✕. Its `header` takes a `FilterPanelTitle`, the chevron, title,
  and actions in the panel's header row. `FilterPanelFooter` is one accent button; Reset moves to
  the header as `FilterResetAction`. `FilterTokens.contentSlide` and
  `closeSwipe` move to `PanelTokens` as `sheetContentSlide` and `sheetCloseSwipe`.
- `PageScaffold` and `Subpage` take a `search`: a search button in the header grows leftward into
  a field over the title (`ExpandingSearch`, `rememberSearchExpansion`) and dims the body until
  something is typed.
- Breaking: remove `SettingsEntry`. `SettingsScreen` takes an `index: List<SettingsItem>` built
  with `settingsIndex { page(title, open) { section(title) { setting(…); toggle(…) } } }`, where
  `open` receives the found setting's title, or null for the page's top. The home lists the
  top-level pages. A search lists every matching page, setting, and option, ranked by name, then
  option, then path, and grouped under a `SettingsGroupHeader` that opens their page. Apps replace
  each `SettingsEntry(title, summary, icon, onOpen)` with
  `page(title, { onOpen() }, icon, summary = summary)` and add their pages' settings inside it.
- Add `appearanceIndex` and `appIconIndex`, the search entries of the Appearance page, and
  `List<SettingsItem>.search`.
- Add `LocalFoundSetting`. While it names a `SettingRow`'s title, that row scrolls into view and
  flashes once, so a search result shows where it opened.
- Add `Modifier.belowControlPanel()` and `Modifier.pageSideInset()` for pages whose control panel
  draws behind the status bar.
- Breaking: remove `SearchPrompt`, replaced by `PanelSearchPill`, and
  `PanelTokens.compactChipPull`. `SearchField` shows its placeholder in `text2`.
- Add `ButtonPair`, a secondary and a primary action in one outlined pill whose primary half
  widens for a long label. `ConfirmDialog`, `ChangeLogSheet`, `BirthdayPicker`, and
  `FilterPanelFooter` use it. `FilterPanelFooter` is now 48 dp tall like other buttons.
- A disabled primary `ActionButton` now has a grey `bgLight` fill and a neutral outline.
- `EmptyState` takes a nullable `description` and leaves the line out when it is null.
- Breaking: remove `ActionDialog`, `DialogAction`, and `InputDialog`. Use `ClosableSheet` with a
  `SheetConfirm` for an explanation with one action, and `InputSheet` for a text prompt.
- Breaking: move `FilterTokens.progressSize` and `progressStroke` to `PanelTokens`, and remove
  `FilterTokens.footerButtonHeight` and `footerTextSize`.
- Accounts: the replace-avatars question is a `ClosableSheet` without its explanation line.
  Settings: Rename theme is an `InputSheet`.
- Breaking: replace `AvatarOptions.domainIconProxy` and the `AvatarService` `iconFallback` parameter
  with `AvatarOptions.iconProxy`, an `IconProxy` of HTTPS URL templates. While one is set, website
  icons come only from the proxy, never from the sender's website; a null `iconUrl` loads none.
  A 1×1 PNG counts as no image. Add `isPublicAvatarUrl`, the public loader's URL check.
- Add brand logos: `IconProxy.brandLogoUrl` and `AvatarRequest.brandDomain`, the From domain a
  message passed DMARC for. See [brand logos](docs/avatar.md#brand-logos).
- Add `RadioChoiceSheet`, a `SheetFrame` of radio rows that saves the picked option on Done, for
  choices that should not apply while the user is still picking. `below` adds content for the
  picked option, and `doneEnabled` can hold Done until it is complete.
- Add `SidebarTileSize` (Small, Wide, Full) and `SidebarTileEditor`, a Control Center style editor
  for the sidebar's mailbox tiles: hold a tile to move it, drag its corner arc to resize it, and tap
  its minus to remove it. `SidebarMailboxGrid` takes `sizeOf`, places tiles in rows in order, and
  no longer adds a gap above itself. `SidebarMailboxTile` takes `size`, and every size is equally tall.
- Breaking: `SidebarInboxEntry` loses `inset` and always spans the full width; put the Inbox in a
  `SidebarMailboxGrid` as a `SidebarMailboxTile` instead.
- A leading row icon that is a plain folder and has a `LocalIconTint` is now filled, as in the
  sidebar's folder tree, so pinned folder tiles and setting rows show folder colors the same way.

- Add `DateFormatter.formatDayMonth`, the saved date format without the year, such as 04.10 for
  DD.MM.YYYY. The device format uses the locale's own day and month order.
- Group JSContact's internal vCard conversion and validation code into `vcard` and `validation`
  packages. Public APIs, including `Card`, `VCard`, and `isEmailAddress`, keep their packages and behavior.
- Breaking: rename the `push` module and `com.otus:push` artifact to `jmap-push` and
  `com.otus:jmap-push`. Replace `com.otus.push` imports with `com.otus.jmap.push` and use
  `libs.otus.jmap.push` with the `otus-jmap-push` catalog alias. Push behavior and stored records
  are unchanged. See [push subscriptions](docs/push.md#module-rename-in-0100).
- Breaking: move design-system elements and components into `shared/<group>` and
  `mail/<feature>` packages. Group theme declarations under `theme.colors`, `theme.motion`,
  `theme.brand`, `theme.typography`, and `theme.tokens.<family>`. Update imports with the
  [package migration guide](docs/ui-package-migration.md). The Maven module, `theme.OtusTheme`,
  resource identifiers, token values, and rendering behavior are unchanged.
- `SidebarInboxEntry` shows its count in the mailbox tiles' badge while `inset`, so the Inbox count
  matches the tiles below it. Pass a shortened count, such as "999+", as for `SidebarMailboxTile`.
- Add `RowMark.Invite`, a calendar mark for a row that carries a calendar invitation. Exhaustive
  `when` over `RowMark` needs a branch for it.
- Breaking: `ContactHeader` takes `onEdit: (() -> Unit)?` instead of `photoChoices`. With it, the
  avatar wears a pen badge and a tap runs it; open your own avatar editor there, such as
  `AvatarSheet`, and offer removal through that editor.
- Breaking: `ConnectedAccountRow` takes `accountAvatar`, the account's avatar, which leads the row in
  place of the @ icon, and `onEditAll`, which runs on a tap of it.
- Add `AvatarSheet`, the avatar editor of the accounts module, now public and renamed from
  `OwnAvatarSheet`, so contacts can reuse it. It takes a `label` and a `domain` instead of an
  address.
- Accounts with several addresses can give all of them one avatar. Before replacing an address's own
  avatar, the editor lists those addresses with their avatars so each can keep its own.
- Add a `SettingRow` overload with a `leading` slot, `ActionDialog(content)`, `AvatarButton`, and
  `FeaturedAvatars` with `FeaturedAvatar`, the large faces with labels above a list.
- Add `Card.withAvatarStyle(style)`, which sets or clears the avatar style of any contact card.

- Add `InfoTip` and `InfoText`: a quiet question mark that opens a sheet with background a setting
  or section explains on request. `SettingsSection` and `SettingToggleRow` take an `info` that places
  one after the title. Email accounts and the custom accent sheet move their long
  notes behind it, and the Password field shows the app-password hint.
- Breaking: `IconButton` takes `look: IconButtonLook` instead of `outlined`. Replace
  `outlined = false` with `look = IconButtonLook.Plain`, and drop `outlined = true`, since
  `Outlined` is the default. `IconButtonLook.Bare` also drops the fill, as the ✕ of a form field
  does.
- Breaking: `PickRow` takes `layout: PickRowLayout` instead of `subtitleLines`. Replace
  `subtitleLines = n` with `layout = PickRowLayout(subtitleLines = n)`. `PickRowLayout.density`
  set to `RowDensity.Compact` makes the row 48 dp tall. The unchosen radio and check now draw a
  `text3` outline, and a chosen check fills with `accentDark`.
- Breaking: `InboxTokens.rowGap`, `cardOutline`, `compactPaddingVertical`, `compactPaddingStart`,
  and `compactPaddingEnd` are removed. Use `EntryTokens.gap`, `outline`, `paddingVertical`,
  `paddingStart`, and `paddingEnd`. `InboxTokens.compactAvatarSize` stays and equals
  `EntryTokens.avatarSize`.
- Add `EntryCard` with `EntryTaps` and `EntryTokens`: the card behind each mail or person in a
  list. Selected neighbors join into one accent block; `rowSelection(keys, index, selected)`
  works out each entry's `RowSelection`. `CompactContentRow` now draws on it.
- Add the contact page components: `ContactEntry` (with `ContactEntryEnd` and
  `ContactRowLabel`) for a list of people, `ContactRow` for a person in a sheet, `ContactHeader`,
  `ContactValueRow`, `ContactFieldRow` (with `ContactFieldLook` and `PrimaryStar`),
  `ContactInputField`, `ContactPickerField`, `FieldLabel`, and `RemoveFieldButton`. The label
  chip, star, and ✕ of a `ContactFieldRow` each have their own 48 dp touch target.
  `AvatarTokens` adds `featuredSize` and `featuredWidth`.
- Add `LetterIndex` with `LetterIndexState`, `rememberLetterIndexState`, `IndexLetter`, and the
  `letterIndexHandle` modifier, a jump-to-letter index that opens from a list's letter headings.
  Screen readers open it with the heading's "Show letters" action.
- Add `BirthdayPicker` and `YearlyDate`, wheels for a yearly date whose year may stay "No year".
  A stored year more than 120 years back or in the future stays on the wheel.
- Add `InputKind.Phone`, `OptionsItem.Action(current)`, which fills the current choice with the
  accent, `BarContext.choices`, which makes the context a button that opens other choices,
  `RowGroup(raised)`, and `ValueButton(outline)`. Add the symbols `symbol_badge`, `symbol_call`,
  `symbol_call_merge`, `symbol_cloud_off`, `symbol_contact_page`, `symbol_contacts`,
  `symbol_group_add`, `symbol_group_remove`, `symbol_help`, `symbol_label`, `symbol_lock`,
  `symbol_person_edit`, `symbol_person_search`, `symbol_share`, `symbol_star`, and `symbol_upload`.
- A `Toast` wraps to three lines instead of cutting its text off. `ToastHost` stacks toasts of
  different heights, older ones peek out 10 dp instead of 6 dp, and an older toast eases into its
  stacked place as a new one arrives.
- Add `VCard.parse` and `VCard.write`, with `VCardImport` and `VCardFailure`, to read vCard 2.1,
  3.0, and 4.0 files and write vCard 4.0 (RFC 9555). See [contacts](docs/contacts.md#vcard). Add
  `isEmailAddress` and `cardPropertyNames` to `jscontact`, and `contactCardPropertiesWithVCard`
  to `jmap-contacts`.
- `AvatarService.saved` takes a nullable `avatar`. Null, as for a removed or linked photo, drops
  the cached result, so the next lookup asks the server again. The `avatar` module adds
  `AVATAR_PHOTO_KEY`, `Card.isAvatarCard`, `Card.avatarStyle`, and `Card.withoutAvatarStyle()`.
- An own address's new avatar shows right after the first save. `JmapOwnAvatars` hands the saved
  avatar to the new `AvatarService.saved` instead of reading it back with a contact search, which
  a server that indexes in the background, such as Stalwart, may not answer with a new card yet.
  It also remembers the cards it makes, so a quick second save updates the card instead of making
  a duplicate.
- On Android, `HtmlView` turns off Safe Browsing, which can send the addresses of remote images to
  Google, and fetches remote images with a reduced User-Agent that names neither the device model,
  the Android version, nor the WebView build.
- On iOS, `HtmlView` keeps each view's cookies and cache in memory, so what a message's remote
  images leave cannot follow the user to other messages. Without `loadRemote`, a WebKit content
  rule list also blocks every web request, as Android's `blockNetworkLoads` does, beneath the
  Content Security Policy.
- Add `PushSubscriptions.beforeCreate` so Apple delivery can buffer verification messages without
  displaying a new-mail alert, including when an expired subscription must be replaced.
- Users can give each of their own addresses an avatar: a photo, the website icon of its domain,
  or an icon or up to three letters on a folder color or a custom color. The `avatar` module adds
  `OwnAvatars` and `JmapOwnAvatars`, which keep it on a self card in the user's address book
  (RFC 9610), plus `AvatarStyle`, `AvatarMark`, `AVATAR_STYLE_PROPERTY`, `OwnAvatarException`, and
  `AvatarPhotoPicker`. See [avatars](docs/avatar.md). `avatar` now depends on `jmap-contacts`.
- Breaking: `ContactPhotoSource` becomes `ContactAvatarSource`, whose `avatar` returns a
  `ContactAvatar` (`Photo` or `Styled`), and `JmapContactPhotoSource` becomes
  `JmapContactAvatarSource`. Wrap an image in `ContactAvatar.Photo` to migrate. `ResolvedAvatar`
  and `StoredAvatar` gain `style`. Contact lookups also ask for `vCard` and search writable address
  books, since Stalwart leaves the user's own books unsubscribed; contact photos in own books now
  show up.
- Breaking: `AccountSetupScreen` takes an `OwnAvatarsState`. Each account lists its addresses as
  pills with their avatars, and tapping one opens a sheet that edits the avatar or resets it to
  the generated one. `OwnAvatarsState` runs saves one at a time. The account row's
  detail no longer repeats those addresses. "Connect an account" and the avatar note stay pinned
  at the bottom of the page. `accounts` now depends on `avatar` and adds
  `ResolvedAvatar.toChipAvatar`, `AvatarStyle.toChipAvatar`, and `ConnectedAccount.ownAddresses`.
- `PageScaffold` and `Subpage` take a `footer` that stays at the bottom below the scrolling body.
- `Avatar` and `AvatarBadge` take a `symbol` and a `content` color, and `AvatarBadge` an `image`
  and a `fill`. `ChipAvatar` takes a picked `FolderColor` or `customColor` and a `symbol`;
  `PersonAvatar` draws them and the new `PersonBadge` draws a `ChipAvatar` as a badge. Add
  `SymbolSwatch`, `AvatarSymbols` with 71 icons, and `AvatarTokens.editorSize`.
- `ColorSheet` moves from `settings` to the design system's components and becomes public, so other
  features can pick a custom color.
- Add the Material Symbols behind `AvatarSymbols` that the bundle lacked, such as `symbol_home`,
  `symbol_pets`, `symbol_rocket_launch`, and the filled shapes `symbol_circle_fill` and
  `symbol_star_fill` (Material Symbols Rounded, Apache 2.0).
- `JmapClient` implements `AutoCloseable`. Closing releases its HTTP client while injected
  engines remain caller-owned.
- Finish connection invalidation and account-change cleanup after credential removal, even
  when the caller cancels the operation.
- `OptionsMenu` and the menus of action bar buttons, such as ⋯, open at once instead of unrolling.
  `Dropdown` takes `unrolls`, which is true by default. Add the `symbol_self_improvement` drawable.
- Add the parts of the compact filter panel: `FilterRowCard`, `FilterValueRow`, `PeopleLine`
  with `RemovablePersonChip`, and `FilterPickRow`, plus `FilterTokens` for their sizes and
  `FilterTokens.FOLDER_MAX_DEPTH`.
- Add `FilterNote` for a small note under a filter page's choices, and `FolderStackChip` (with
  `StackedFolder`), the pill for chosen folders. `AvatarStackChip` takes a `prefix`, such as "To".
- `FilterPanel` always takes its full height, so short pages keep the height of long ones.
- `FilterPanel` draws a plain ✕ at its top end that calls `onDismiss`. Its footer floats at the
  bottom, and the body scrolls under it and fades out over `PanelTokens.fadeWidth` just above it.
  A disabled `FilterPanelFooter` button dims only its label, so the pill hides the body behind it.
- `FilterPanelHeader` drops `onClose` and `closeDescription`, since the panel draws the ✕. Remove
  those arguments; the header keeps room for the ✕ at its end.
- `RecipientLine` with the default `collapsedAfter` no longer folds into a "+0 more" chip, which
  hid its address field.
- Add `DateRangeCalendar`, the calendar of `DateRangePicker` on its own, to embed in a page.
- `FilterFolderRow` ends in the same check circle as `FilterPickRow` instead of a checkbox.
- Add the `symbol_check_circle_fill`, `symbol_radio_button_unchecked`, `symbol_hourglass_empty`,
  and `symbol_account_circle` symbols (Material Symbols Rounded, Apache 2.0).
- Add the `jscontact` module (`com.otus:jscontact`, decision 0016) for JSContact cards
  (RFC 9553): `Card` and one class per object type, such as `Name`, `EmailAddress`, `Phone`,
  `Address`, `Media`, and `Anniversary` with `PartialDate` and `Timestamp`. Every class keeps
  unknown and vendor-specific properties in `extra` and writes them back (sections 1.7.4 and
  1.8.1). Enumerated values such as `CardKind`, `ContactContext`, and `PhoneFeature` are value
  classes that keep values they do not know. Every `Card` property is optional, so partial cards
  from a JMAP `/get` decode, and a missing `version` reads as `1.0`. See
  [contacts](docs/contacts.md).
- Add `Card.validate`, which returns a `JsContactProblem` with a JSON Pointer for each rule of
  RFC 9553 or RFC 9610 the card breaks.
- Add `Card.localized` and `JsContactPatch.apply`, which apply a localization PatchObject
  (RFC 9553, sections 1.4.3 and 2.7.1).
- Add the `jmap-contacts` module (`com.otus:jmap-contacts`, decision 0016) for JMAP for Contacts
  (RFC 9610): `ContactCardType`, `AddressBookType`, `AddressBook` with `AddressBookRights` and
  `AddressBookOrder`, and `JmapAccount.contacts`, which reads `ContactsCapability`
  (section 1.4.1). No app uses it yet.
- Add `AddressBookSetMethod`, `NewAddressBook`, and `addressBookPatch` for `AddressBook/set`, with
  `onDestroyRemoveContents`, `onSuccessSetIsDefault`, and per-Principal `shareWith` changes
  (RFC 9610, section 2.3).
- Add `ContactCardFilter` and `ContactCardSort` for `ContactCard/query` (RFC 9610, sections 3.3.1
  and 3.3.2), and `contactCardPatch` and `Card.toCreation` for `ContactCard/set` (section 3.5).
- Add `QueryChangesMethod`, `QueryChangesResult`, and `AddedItem` for `/queryChanges` (RFC 8620,
  section 5.6).
- Add `CopyMethod` and `CopyResult` for `/copy` (RFC 8620, section 5.4). The client checks the
  type's capability in both accounts before sending.
- Add `JmapFilter`, `FilterOperator`, `allOf`, `anyOf`, `noneOf`, `and`, and `or`, which build
  `/query` filters with FilterOperators (RFC 8620, section 5.5).
- Add `jmapPatch`, `JmapPatchBuilder`, and `patchPath`, which build a `/set` PatchObject with
  escaped JSON Pointer keys and reject overlapping paths before sending (RFC 8620, section 5.3).
- Add `JmapSort.collation`, sent only when set, and `QueryResult.canCalculateChanges` and
  `QueryResult.limit` (RFC 8620, section 5.5). A `limit` outside 0 to 2^53 - 1 fails decoding.
  Existing calls compile unchanged.
- Add `JmapSession.maxObjectsInGet`, read from the core capability (RFC 8620, section 2).
- Add `UtcDateSerializer`, which reads and writes a UTCDate (RFC 8620, section 1.4) as a
  `kotlin.time.Instant`. It writes the `Z` form without trailing fraction zeros, such as
  `2014-10-30T06:12:00.5Z`, and reads any RFC 3339 offset. A leap second reads as the last
  nanosecond before it, and a year outside 0000 to 9999 fails in both directions.
- Add the `com.otus.jmap.sharing` package for RFC 9670: `Principal`, `PrincipalKind`,
  `PrincipalAccount`, `PrincipalType`, `PrincipalFilter`, `ShareNotification`, `ShareEntity`,
  `ShareNotificationType`, `ShareNotificationFilter`, `ShareNotificationSort`,
  `PrincipalsCapability`, `PrincipalOwner`, `JmapAccount.principals`, and
  `JmapAccount.principalOwner`. Add `JmapCapability.PRINCIPALS` and
  `JmapCapability.PRINCIPALS_OWNER` (sections 1.5.1 and 1.5.2). `Principal.type` and the
  properties RFC 9670 requires on a `ShareNotification` are nullable, so a `/get` with
  `properties` that leaves them out decodes.
- Add the read view parts. Strips above the action bar: `StripCard`, `PositionStrip` with
  `PageDots`, `ThreadList`, `AlsoInStrip` with `FolderHint` and `FolderName`, `InfoStrip` with
  `StripAction`, and `AttachmentStrip` with `AttachmentFile`, `AttachmentKind`, and
  `AttachmentActions`. In the body: `ImageBanner`, `DetailsTable` with `DetailLine`, and
  `QuoteFold`. In the header: `DatePill`. Also `FolderPath`, `FolderStack`, `PlainAction`,
  `FirstFit`, which shows the first of several versions of a slot that fits, `PersonChip`, a person
  in the `RecipientChip` pill that is only shown or is a button with an `onClick`, and
  `FolderPickRow`, a folder with a checkbox for the folders sheet, 48 dp tall. `PositionStrip`
  always shows its dots and position text in full, and takes a `description` that replaces what
  screen readers hear. `SlidingPositionStrip` takes a `PositionPage` for each page and a
  position, and slides each page's content inside a card that stays put, as a mini player turns
  its tracks. `Unfold` tracks how far a strip has unfolded, follows a finger with `dragBy`, and
  eases to rest; `UnfoldingStripCard` grows with it, and `AttachmentStrip` takes an `Unfold` in
  place of `expanded`. `SheetPull`, provided as `LocalSheetPull`, lets a finger outside a sheet pull
  it up. `QuoteFold` takes a `QuotedLevel` and the `deeper` levels it quotes, each nested one step
  further in; `HtmlQuote` takes `deeper` `HtmlQuoteLevel`s and folds them the
  same way in HTML bodies. Add `ReadTokens`.
- `PickRow` takes `subtitleLines` for a description longer than one line, and `PickMark` takes
  `atStart`, which puts the radio or checkbox before the text, as in a settings list of choices.
  `subtitleLines` comes before `leading`, so a call that passes `leading` by position, not as the
  trailing lambda or by name, must name it.
- `AddressPill` takes `expanded`, which turns its chevron up while what it opens shows.
- Add `DestinationPill`, a 40 dp pill for the Move to sheet, and `MoveTokens`. `IconPill` takes a
  `size`, so the sort button can match it; it stays 36 dp by default.
- `OptionsMenu` and `OptionsList` show `OptionsItem.Caption`, a small heading row.
  `OptionsItem.Action` takes a `detail`, shown as a second line in `MenuTokens.detailSize`, and so
  does `OptionsItem.Toggle` through a new constructor that puts `detail` before `onCheckedChange`.
  The old constructors still work.
- `HtmlView` takes `onShowRemote`: while remote content is blocked, each remote image becomes a
  dashed placeholder of its size, and tapping one runs `onShowRemote`. Only the placeholder link
  of the current render counts, so a link in the message cannot fake it. It also takes a
  `HtmlQuote`, a data class, folded under the body in a `<details>` element with its author and
  date, each left out when blank, and unfolded by a tap without scripts. An element the body or
  quote leaves open, such as `<style>`, `<a>`, or a comment, is closed before the fold.
- Add the `symbol_expand_less`, `symbol_unsubscribe`, `symbol_download`, `symbol_hide_image`,
  `symbol_image`, `symbol_verified_user`, `symbol_block`, `symbol_reply_all`, `symbol_print`,
  `symbol_info`, `symbol_notifications_off`, `symbol_delete_sweep`, `symbol_unfold_less`,
  `symbol_landscape`, `symbol_grid_on`, `symbol_slideshow`, `symbol_folder_zip`,
  `symbol_music_note`, and `symbol_movie` symbols (Material Symbols Rounded, Apache 2.0), and
  `symbol_pdf`, the "PDF" letters of `picture_as_pdf` on their own. `AttachmentKind` also covers
  slides, archives, audio, video, calendar events, contacts, and code, and files sit on round
  tiles, like avatars.
- `IconButton` takes a `badgeCount` and draws it outside its circle, so the filter button's count
  is no longer cut off. It replaces `BadgedSymbol`, which is removed.
- A swipe reveal shrinks its edge fillet and free-end corner together while it is narrow, so it
  stays rounded from the start instead of growing out of a square sliver.
- Round corners more, from one internal scale in `CornerTokens`. The action bar, menus,
  dropdowns, and sheet and dialog tops go from 22 dp (context menus from 16 dp) to 30 dp. Rows,
  cards, row groups, and sidebar entries go from 16 dp to 24 dp, chips and value buttons from 8 dp
  to 14 dp, formatting buttons from 12 dp to 20 dp, and the formatting row from 22 dp to 28 dp.
  Smaller radii move up one step. The control panel and `FilterPanel` keep 22 dp. Public tokens
  such as `SheetTokens.rowRadius` and `ThemeTokens.previewRowRadius` keep their names with the
  new values.
- Add `ChangeLogSheet`, which lists `ChangeLogSection`s of bulleted changes over a secondary and a
  wide primary button, such as Changelog and Install update, and `ChangeLogHistorySheet`, which
  lists `ChangeLogRelease`s by title. Installed releases share a lighter panel under an
  "Installed" divider.
- `SidebarFooter` takes `updateProgress`: a ring fills around the update arrow while the update
  downloads, and tapping does nothing. The arrow now sits in a `bgRegular` circle in the version's
  `text2` color.
- The `SidebarOwl`'s eye glints take the accent color, like its body.
- Add the filter panel parts: `FilterPanel`, which grows out of a control panel over a scrim,
  `FilterPanelHeader`, `FilterPanelFooter`, `FilterSection`, and `FilterFolderRow` (with
  `FolderCheck` and `TreeFolder`), and the `FilterChip` (with `FilterChipRole` and `ChipLabel`)
  and `BadgedSymbol` elements. Add `FilterTokens`. The panel's disabled parts use
  `OtusAlpha.DISABLED`.
- `FolderGlyph` takes `color` first, with `kind` and an `icon` for uncolored folders, so filter
  chips can show a system folder's own symbol. It is a breaking change for 0.8.0 callers that
  passed `kind` first.
- `SegmentedToggle` takes a `height`; it stays 48 dp by default.
- Add the `symbol_calendar_today`, `symbol_person_add`, `symbol_forward`, `symbol_event`,
  `symbol_newspaper`, `symbol_folder_off`, `symbol_speaker_notes_off`, `symbol_swap_vert`, and
  `symbol_straighten` symbols (Material Symbols Rounded, Apache 2.0).
- Add `CompactContentRow`, a two-line list row for mail conversations, with its model
  `CompactRowText`, `RowLine`, `RowTextPart`, `RowMark`, `RowFolderIcon`, and `CountPlacement`.
  It selects like `ContentRow`, and `dimmed` fades read rows without fading the selection tint.
- Add `AvatarBadge`, and `BadgedAvatar`, which puts a badge on an avatar's bottom-end corner.
- Add `CountBadge`, a count in an avatar-style circle, such as unread mail beside a pill's label.
- Add `CountPill`, `FolderGlyph` with `FolderGlyphKind`, and `FolderColorNudge`, a card that
  offers a color for a folder that has none.
- Add `OtusAlpha` with `DIMMED` and `DISABLED`; `PickRow` uses `DISABLED`.
- Add `NudgeTokens`, the compact row and badge sizes in `InboxTokens`, and the count pill sizes
  in `LabelTokens`.
- Add the `symbol_reply`, `symbol_folder_fill`, `symbol_folder_open_fill`, and
  `symbol_bookmark_fill` drawables.
- A sidebar folder with a color shows a filled folder icon. One without a color shows an outlined
  folder in text3.
- `ContextPanel` takes `compact`, which shortens the header and pulls the chips up, such as while
  a list scrolls down. `PullToRefresh` takes `indicatorInset` for content drawn under a panel.
- `SidebarOwl` draws the owl in the accent color.
- `RichText` holds images through `RichText.images`: `insert` puts one at the cursor, `remove`
  takes it out, `sources` lists them, and `selected`, `width`, `resize`, `description`,
  `describe`, and `replace` edit the selected one. `RichTextBody` shows each between the text
  around it through its new `images` slot, since a text field cannot draw images.
- Add `ImageBar`, the bottom bar while an image is selected: size presets, ⋯ options, and ✓.
- Add `ExpandingBarGroup`, a bar group that grows upward into a panel above its row, and
  `OptionsList`, the rows of an `OptionsMenu` for any surface. `ImageBar`'s ⋯ uses both.
  `ResizableImage` draws dashed preset guides with a haptic tick while its handle drags.
- **Breaking:** `BarMorph` takes the bar to show and a `content` lambda for it, instead of two
  slots, so it can morph between any bars. `ResizableImage` drops `onRemove` and its size pill;
  `ImagePreset` gains `short`. `SegmentedToggle` takes a `fontSize`, and keeps labels on one line.
- `SignatureTokens.snapDistance` is 12 dp.
- Add `decodeImage(bytes, maxSize)` and `rememberImage`, which decode a photo at a size of the
  caller's choosing; `decodeAvatarImage` is `decodeImage` at avatar size.
- `AttachmentChip` takes `actions`, shown in a `ContextMenu` on a tap, instead of `onRetry`.
- Add `ComposeTokens.inlineImageLarge`, `guideReach`, and `guideDash`.
- Add `ComposeTokens.inlineImageLoadingHeight`, `inlineImageMinWidth`, `inlineImageSmall`, and
  `inlineImageMedium`.
- **Breaking:** `RichTextBody` moved from `elements` to `components`, since it draws
  `ResizableImage`. `ResizableImage` takes an `EditableImage` (painter, description, status,
  actions) instead of an `ImageBitmap`, and `ImageSizing` takes a `range` before `onWidthChange`.
- Add `AttachmentChip`, a file on a message being written with its name, a detail such as its
  size, a ✕, and an optional retry, and `ComposeTokens.attachmentMaxWidth`.
- Add `BarMorph`, which morphs a `FormattingBar` into a full `ActionBar` and back. Each part takes
  its match's height, then its width, so the row splits into the group and shell. The content
  blurs while the width changes. It runs at `MotionSpeed.Normal` and follows `LocalMotionScale`.
- Add `isKeyboardUp()`, which turns false as soon as the keyboard starts to close. On iOS it
  stays true until the keyboard is gone.
- A `FormattingBar` spans the width above the keyboard, and draws its border under its buttons.
- **Breaking:** `CompactSendButton` fills the formatting row's height with the action bar's
  collapsed action shape, flush with the row's end. `ComposeTokens.sendButtonWidth` is removed.

- A `SheetFrame` never gets shorter than it has been, so its top stays put when the keyboard
  closes. A sheet opened from another sheet's content starts at least as tall.
- A row dragged in a `ReorderableTree` takes its nested rows along. Before, a parent hovering over
  its own nested rows showed no drop line.
- `JmapClient.download` and `upload` refuse a `.` or `..` template value, which would move the
  request to another path on the server. A `.` or `..` file name falls back to `download`.
- `HtmlView` without `loadRemote` turns off DNS prefetching and makes the document's `<link>`
  elements inert, since the Content Security Policy does not stop `dns-prefetch` or `preconnect`
  hints from reaching the sender's server.
- `ConnectOutcome.ConfirmHost.host` includes the port when it is not the scheme's default, so the
  user sees which port approving it trusts.
- `SlidingPages` and `SlidingOverlay` keep a `ContextPanel` or an `ActionBarHost` bar still
  when the pages on both sides of a slide have one. During the slide the slider draws that
  chrome above both pages, so only the content under it moves.
- Add `SettingPageRow`, a `SettingRow` that opens a page and ends in a chevron, with the page's
  value as its description. `SettingsScreen` uses it, and so does the Theme row on
  `AppearanceSettingsScreen`, which names the theme there instead of in a value button.
- Add the `symbol_content_copy` icon.
- **Breaking:** `AppearanceSettingsScreen` is titled "Appearance" and drops its `content` slot.
  Apps put behavior settings on a page of their own.
- Add `ContextPanelScope.fillRail`, which stretches a chip across the chip rail. A `PickerChip`
  given a width fills it and puts its chevron at the end.
- Add `Modifier.actionBarInset`. `ActionBarHost` uses it to keep its bar as far from the screen's
  sides as from its bottom edge, which includes the navigation bar's height.
- A `SettingRow` at the top of its parent, such as the first row on a page, no longer draws
  a hairline above it.
- Add `AndroidAnimationPreferenceStore` and `IosAnimationPreferenceStore` in `com.otus.settings.data`,
  platform stores for `AnimationSettingsState`, so both apps share them.
- **Breaking:** `AnimationPreferenceStore` moves to `com.otus.settings.domain`, so the data-layer
  stores do not depend on presentation.
- `PushSubscriptions` never subscribes an endpoint without encryption keys; such a login shows
  `PushStatus.Refused`. Without keys a server would push readable state changes through the push
  provider, and the app could not tell forged pushes from real ones.
- `PushSubscriptions.sync` keeps every record as it is when the saved logins cannot be read, and
  keeps a login's record when its secret cannot be read, instead of throwing `LoginStoreException`.
- `PushSubscriptions.forget` gives up after ten seconds, including time waiting for a running
  `sync`, so removing an account never hangs on a slow or unreachable server.
- Add `HtmlView`, which shows an HTML document such as a message body in a platform web view.
  Scripts never run. A Content Security Policy blocks frames, forms, and every outside resource
  except remote images, styles, and fonts, which load only with `loadRemote`. Tapped web, mail,
  and phone links go to `onOpenLink`; the view never navigates. Colors follow the theme the user
  picked: a document without its own colors takes the theme's text and link colors, and one with
  its own colors keeps a white page, darkened under a dark theme on Android 13 and later.
- Add `TrailingIcons`, two touch-target-wide slots at a row's end. `FolderSettingRow` and
  `AccountGroupRow` use it, so their icons line up in the same two columns.
- Add `InputSheet`, `InputDialog`'s text field in a `SheetFrame`.
- Add `QuickActionBar`, a small bar of icon `MenuAction`s that opens above a long-pressed point.
  `MenuAction` takes an optional `icon` before `onClick`. `FolderSettingRow` takes `onLongClick`
  with the pressed point, and its drag handle now comes from the `ReorderableScope` overload
  instead of a `handle` parameter.
- `FolderSettingRow` ends in a settings icon instead of a chevron.
- `FolderSettingRow` draws tree guide lines under the icons of the levels above it, and
  `AccountGroupRow` takes an `action`, such as a New folder button, before its chevron.
- Add `ReorderableTree` and `TreeDropTarget`. A dragged row stays over the rows without moving
  them, and an `accentLight` line shows whether it lands in a gap, at the depth of the row below,
  or inside a row as its first child. The app's `canDrop` hides targets it would refuse.
- `ReorderableColumn` fills the row being dragged with `bgLight`; the other rows step aside. `FolderSettingRow` takes a
  `handle`, such as `ReorderableScope.DragHandle`, after its chevron, and `SidebarFolder` takes the
  `icon` that `FolderSettingRow` took before.
- Add `Modifier.mutedRule()`, which fades a `RuleChip` or `RuleChipGroup` shown only for context,
  such as a parent folder's rule.
- Add `Modifier.nestIndent(step, depth)`, which indents a nested row by at most four steps
  (`MAX_INDENT_STEPS`). `SidebarEntry`, `SidebarFolderEntry`, `DetailRow`, and `FolderSettingRow`
  use it, so rows deeper than the fifth level stop moving right.
- Add `RuleChip`, `RuleChipGroup`, and `AddRuleChip` for mail rules, with the value types
  `RuleChipLine`, `RuleChipValue`, `RuleSegment`, and `RuleSegmentStyle`. A chip is 36 dp tall, or
  24 dp with `RowDensity.Compact`.
- Add `FolderSettingRow` for a folder settings page, with the detail lines `FolderRuleSummary` and
  `FolderOwnerLine`, and `PickerChip`, a header chip with avatars, a label, and a down chevron
  that opens a picker. `Subpage` and `PageScaffold` take optional `chips` for the header's chip row.
- `ReorderableScope` adds `Modifier.dragOnLongPress()`, which drags a row after a long press
  without a handle. Apps that implement `ReorderableScope` themselves must add it; the design
  system is the only implementer today.
- Add the `symbol_short_text`, `symbol_code`, `symbol_forum`, `symbol_notes`, `symbol_schedule`,
  `symbol_filter_alt`, and `symbol_folder_shared` symbols.
- **Breaking:** `Sidebar` has no account rail. It takes a `header` slot in place of `subtitle` and
  `rail`, and `title` now only names the pane for screen readers. `SidebarRail`,
  `SidebarRailAccount`, `SidebarRailIcon`, `SidebarRailAdd`, and `SidebarRailDivider` are gone.
  Apps pass a `SidebarHeader` and offer the account choice in a sheet such as `ScopeSheet`.
- Add the sidebar pieces `SidebarHeader`, `SidebarInboxEntry`, `SidebarMailboxTile` with
  `SidebarMailboxGrid`, `SidebarFolderEntry` with `SidebarFolder`, `SidebarDivider`, and
  `SectionCaption`. Add `ScopeSheet`, `ScopeRow`, `ScopeChildRow`, and `ScopeOption` for a
  "Show mail from" style picker with pinned choices and expandable groups. `AvatarStack` takes an
  optional `size`. Add the `symbol_folder_open` and `symbol_keep` symbols.
- Add `FolderColor`: `Default` and eight colors a user can give a folder, stored by `name` and
  read back with `FolderColor.named`. `tint` and `tintIn(palette)` give the icon color for dark or
  light themes, at 3:1 or more on `bgDark`, `bgRegular`, and `accentDark`.
- Add `LocalIconTint`, which colors the leading icon of `SidebarEntry`, `ActionRow`, `SettingRow`,
  `SettingToggleRow`, `ValuePicker`, and `ValueToken`, but not a disabled picker's icon.
  `ChoiceRow` takes `iconTint` and a `swatch`, drawn by the new `FolderColorSwatch`, and
  `ThemeTokens.folderSwatch` sizes it.
- Add `ToastHost` with `ToastState`, `ToastMessage`, and the `Toast` element with `ToastAction`.
  Toasts stack with the newest in front, merge by `group`, fan out on tap, and swipe away in
  either direction. A toast with an action shows a countdown ring, and running the action closes
  the toast. `ToastState.isShowing(group)` tells whether a group's toast is still up, and
  `ToastState.messages` lists the messages on screen. See
  [the design system guide](docs/designsystem.md).
- Add the brand assets: source SVGs in `designsystem/brand/`, `tools/brand_drawables.py`, the
  `brand_otus_logo`, `brand_owl`, and `symbol_owl` drawables, and Mail's launcher icons
  `otus_mail_icon_<name>` in Monochrome and each preset accent as Android resources.
- Add `MailLaunch`, Mail's launch animation in the current theme's colors, which loops while the
  app loads and shrinks and fades out once it is done. It comes with `AppIconStyle`,
  `BrandTokens`, `MotionCurve.Rise`, `Motion.LAUNCH_MILLIS`, `Motion.LAUNCH_LOOP_MILLIS`, and
  `Motion.scaled(millis, scale)`.
- Add `AccentPreset.closest`.
- Add `AppIconSettingsState` and `AppIconSetting`, the App icon row an app that can switch its
  launcher icon adds to the appearance page's Look section.
- Add the Ink theme, darker than Otus with surfaces closer together, and custom themes (decision
  0014). `ThemeColor` names the ten colors a user can change. `ThemeChoice` keeps a list of
  `CustomTheme`s, and `withColor` on a built-in theme creates and selects a new custom theme, so
  built-in themes never change. `withBase`, `withCustomTheme`, `withAccent`, `withName`,
  `withCopy`, and `withoutCustomTheme` select, edit, rename, duplicate, and delete themes.
- **Breaking:** the Graphite theme is removed. A saved choice of Graphite opens as Otus.
- **Breaking:** `AccentShades.adjusted` is gone, and so are the accent card tokens
  `accentCardPadding`, `accentCardGap`, `rolePillHeight`, and `roleLabelSize`.
- **Breaking:** `ThemeBase.title` in `settings` is now internal. Use `ThemeChoice.themeTitle`,
  which also names custom themes.
- Add `SwatchStack`, overlapping color circles like `AvatarStack`. `ValueButton` takes an
  optional `fill` color with readable text on it. `SheetHeaderAction` is now public.
- Add `ContextMenu` with `MenuAction`: a small menu that opens where the user pressed, without
  dimming the screen, and `Modifier.onPressDown` to find that point. `ThemeCard` takes optional
  `actions` and opens them in it from a long press or the selected card's more button.
- The theme page drops the accent shade examples and lists every color under Customize colors. A
  theme's actions duplicate it, and rename or delete a custom theme. "Reset to Otus" keeps custom
  themes.
- The color sheets now close after Done or Reset. Before, the closed sheet stayed on screen,
  dimmed the page, and blocked taps.
- Add `SlidingPages`, which slides a new page in from the side and pushes the current one out.
  A deeper page comes in from the end edge on `MotionCurve.Standard`; a shallower one returns
  from the start edge on `MotionCurve.Reverse`. Both run at `MotionSpeed.Normal`.
- Add `SlidingOverlay`, the same slide for a page over another that stays composed, such as an
  open message over its list.
- Add `AnimationSpeed` (Instant, Slow, Regular, Fast), a user choice of `LocalMotionScale`.
  Instant finishes every `Motion` animation at once. Add `AnimationSettingsState` and
  `AnimationPreferenceStore` to `settings`, and the `symbol_speed` symbol (Material Symbols
  Rounded, Apache 2.0).
- **Breaking:** `AppearanceSettingsScreen` takes `animation` and adds an Animations row to the
  Look section. Apps must pass an `AnimationSettingsState` and provide its speed's scale as
  `LocalMotionScale`.

- Add `MotionCurve.Reverse`, the standard curve played backward. When a page closes, the action
  bar beneath now moves back on it, and a context that goes away in place uses it too, so both
  replay the way the bar collapsed.
- Add the `jmap-push` module (decision 0015). `PushSubscriptions` keeps one JMAP push subscription per
  login pointed at the app's push endpoint: it creates, verifies, renews, replaces, and deletes
  subscriptions, and reports a `PushStatus` per login. Apps store records through
  `PushStateStore`. `forget` deletes a login's subscription before the login is removed, and
  `JmapAccountRepository` takes a `beforeRemove` hook to call it. See [push](docs/push.md).
- Add JMAP push wire types (RFC 8620, section 7, and decision 0015): `PushSubscriptionGetMethod`,
  `PushSubscriptionSetMethod`, `PushSubscription`, `NewPushSubscription`, `PushKeys`, and
  `PushMessage` with `StateChange` and `Verification`. `JmapCapability.WEBPUSH_VAPID` and
  `JmapSession.vapidPublicKey` read the server's VAPID key (RFC 9749). `SetResult.newState` and
  `GetResult.state` are empty for these state-less calls.
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
- The avatar module refuses more private and special-use hosts in every check that uses the shared
  host rule: `HttpAvatarImageLoader` and `HttpWebsiteIconSource` requests and redirects,
  `avatarIconDomain`, `PublicSuffixDomainResolver`, and `AvatarService` domain icons. Besides
  `localhost`, `local`, `internal`, `test`, and `invalid`, it now refuses `lan`, `home`, `corp`,
  `intranet`, `private`, `localdomain`, `example`, `onion`, `alt`, and all of `arpa`, such as
  `home.arpa`.
  Hosts whose last label is not alphabetic or an `xn--` IDN label are refused too, because
  resolvers read forms such as `0x7f.0x1` as an IPv4 address. Before, a website could redirect an
  icon request, or advertise an icon link, to a host such as `printer.lan` on the user's network.
  `HttpAvatarImageLoader` also refuses HTTPS URLs on any port other than 443. Senders at these
  domains keep their initials. Public names that resolve to private addresses still pass; see
  [the avatar guide](docs/avatar.md).
- `JmapClient.download` and `JmapConnection.download` take a `name`, which fills the download
  URL's `{name}`, the file name the server reports for the blob (RFC 8620, section 6.2). It
  defaults to `download`. Every blob used to be requested as `avatar`; callers that pass no name,
  such as Mail's Sieve script download, now request `download`. `JmapContactPhotoSource` passes
  `avatar`, so contact-photo requests are unchanged. Existing calls compile unchanged.
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
- `JmapAccountRepository.accounts()` asks every saved login's server for its sending addresses at
  the same time instead of one after another, so the list waits for the slowest server rather
  than for each server in turn. Accounts keep their saved order, and a server that fails still
  falls back to its saved sign-in without affecting the others.
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
- `HttpWebsiteIconSource` reads a page's icon links in one linear pass and parses on
  `Dispatchers.Default`, so a hostile page, such as one of thousands of unclosed `<!--`, no longer
  holds the caller's thread for seconds. An unclosed comment now hides the rest of the page, and a
  `</head>` inside a comment no longer ends the head.
- `AvatarService` asks a website that has no icon, or does not answer, again after a day rather
  than after five minutes when it fetches icons from the website itself, so a sender's domain
  learns the device's IP address at most once a day. Contact and icon-proxy misses are still
  retried after five minutes.
- `FileAvatarStore` reads the size and age of its records once and keeps them in memory, so a
  write no longer lists the directory and opens every record. A temporary file left by a killed
  process is deleted on the store's first write. Use one store per directory.
- `VCard.parse` joins the soft line breaks of a quoted-printable value in linear time; a long
  value of many short continuation lines took seconds before.
- `VCard.write` no longer lets a line break split a content line: it percent-encodes control
  characters in MEMBER, PHOTO, and URL values, escapes a lone CR in text and parameter values,
  and leaves out a `kind` that is not a vCard token, such as `example.com:baz`, which then reads
  back as an individual.
- `PushSubscriptions.sync` renews only verified subscriptions. An unverified one is created again
  an hour after creation even when the server grants less than two hours; before, each renewal
  restarted its verification hour, so it was never replaced.
- Decoding a JSContact object throws a `SerializationException` when a member nests arrays or
  objects more than 64 levels deep. Before, such a card decoded but overflowed the stack, with a
  `StackOverflowError`, when it was encoded again.
- Breaking: `ContextPanel` takes `motion: PanelMotion` in place of `collapse`. A `PanelMotion`
  carries the scroll collapse, an optional `PanelExpansion`, and the content the panel grows. With
  an expansion, dragging the panel grows it under its header row, which stays put, and the pills
  fade in place. It never grows past its place, such as above the keyboard.
- Add `PanelExpansion` and `rememberPanelExpansion(stops, content)`: the panel follows the finger
  and settles on the nearest stop, or the next one after a fling, on the decelerating curve for
  only as long as the distance left needs. `expandTo` and `collapse` move it on the motion curve.
  A stop the layout moves, such as with the keyboard, carries a resting panel with it; new
  `content` animates instead. `skipping`, `moving`, `dragging`, `position`, and `expanded` tell
  callers what it does; a panel opened straight to a stop goes straight back.
- `PanelSheet` hands a swipe up from its scrolling content to the sheet once the content ends,
  settles a short swipe back on the motion curve, and closes on a flick up
  (`PanelTokens.sheetCloseFling`).
- `PanelSearchPill` takes a `PillSearch`, which makes the pill itself the search field, its prompt
  the placeholder, with a clear button once text is typed.
- Add `PillRow`, pills in a row that scrolls with the rail's edge fade and a trailing button.
- Add the quick view pieces in `mail/quickview`: `QuickCounts`, `QuickFaces`, `ScrollingFaces`
  (fixed 68 dp slots and a rolling "+N" pill for the faces past its end), and `QuickJump`, with
  `QuickViewTokens`.
- Add `FoldingSection` and `rememberSectionFold`: a caption whose content shrinks, fades, and folds
  away as a list under it scrolls, and unfolds with the list's top or the caption's chevron.
- Add `LocalEntriesOnRaised`, which gives entry cards the page color on a raised surface.
- Add `FilterShowBar`, an `ActionBar` with Show as its primary and Reset filters, the new
  `symbol_filter_list_off`, as its icon.
- `ActionBar` changes a primary's label in place while its symbol stays, and `RollingWords` rolls
  only the changed digits of a number, with a sign such as "+" held still.

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
