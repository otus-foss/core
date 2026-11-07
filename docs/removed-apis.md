# Removed APIs

Public APIs that no code in Mail or Calendar called, outside their own tests, were removed on
2026-11-06 and 2026-11-07 (decision 3 of the untangle refactor). Each entry names the last commit
that has it, on `origin/main` of otus-meta unless the entry names another branch. To bring one back, restore its files from that commit with
`git checkout 2b8a290b -- <path>` and its tests with them.

| API | Module | Removed | Last commit with it | What it did |
| --- | --- | --- | --- | --- |
| `com.otus.jmap.sharing` (`Principal`, `PrincipalFilter`, `ShareNotification`, `ShareNotificationQuery`, the sharing capabilities) | `jmap` | 2026-11-06 | `2b8a290b` | Read and queried RFC 9670 Principals and ShareNotifications. |
| `JmapCapability.PRINCIPALS`, `JmapCapability.PRINCIPALS_OWNER` | `jmap` | 2026-11-06 | `2b8a290b` | The RFC 9670 capability URIs the sharing package used. |
| `CopyMethod`, `CopyResult` | `jmap` | 2026-11-06 | `2b8a290b` | Sent `Foo/copy` (RFC 8620, section 5.4), with `onSuccessDestroyOriginal`. |
| `AddressBookSetMethod`, `NewAddressBook`, `AddressBookPatch`, `addressBookPatch` | `jmap-contacts` | 2026-11-06 | `2b8a290b` | Created, changed, shared, and destroyed address books (RFC 9610, section 2.3). |
| `contactCardPatch`, `CardPatchProperty` | `jmap-contacts` | 2026-11-06 | `2b8a290b` | Built a ContactCard PatchObject one path at a time; `cardPatch(before, after)` replaces it. |
| `ContactCardSort` | `jmap-contacts` | 2026-11-06 | `2b8a290b` | Built the `ContactCard/query` sorts of RFC 9610, section 3.3.2. |
| `ContactsCapability`, `JmapAccount.contacts` | `jmap-contacts` | 2026-11-06 | `2b8a290b` | Read an account's contacts limits, `maxAddressBooksPerCard` and `mayCreateAddressBook`. |
| `Card.validate`, `JsContactProblem`, the `validation` package | `jscontact` | 2026-11-06 | `2b8a290b` | Reported each RFC 9553 rule a card broke, with its JSON Pointer. |
| `DateFormatter.formatIsoDate` | `dateformat` | 2026-11-06 | `2b8a290b` | Formatted an ISO date string; `format(LocalDate.parse(text))` does the same. |
| `ContentRow`, `ContentRowText`, `RowLabel` | `designsystem` | 2026-11-06 | `2b8a290b` | An inbox-style row with title, subtitle, trailing text, labels, and selection; Mail draws `CompactContentRow`. |
| `DetailRow` | `designsystem` | 2026-11-06 | `2b8a290b` | A row of caller-supplied details with tree indentation. |
| `ActionRow` | `designsystem` | 2026-11-06 | `2b8a290b` | A titled row with an optional icon, summary, and trailing symbol. |
| `FilterPanelHeader` | `designsystem` | 2026-11-06 | `2b8a290b` | A filter panel's title row, or a back button named after the previous page. |
| `FilterNote` | `designsystem` | 2026-11-06 | `2b8a290b` | A 12 sp `text3` note under a filter page's choices. |
| `SidebarHeading` | `designsystem` | 2026-11-06 | `2b8a290b` | A collapsing caption over a group of sidebar entries. |
| `FolderPickRow` | `designsystem` | 2026-11-06 | `2b8a290b` | A folder name with a checkbox, for picking folders. |
| `ButtonPair(primaryBusy)` | `designsystem` | 2026-11-06 | `2b8a290b` | A progress ring after the primary label. |
| `ChoiceDialog` | `designsystem` | 2026-11-06 | `2b8a290b` | A single-choice popup; `ChoiceSheet` does the same job as a sheet. |
| `symbol_arrow_downward`, `symbol_collapse_content`, `symbol_contact_page`, `symbol_create_new_folder`, `symbol_folder_off`, `symbol_rule_folder`, `symbol_share`, `symbol_speaker_notes_off` | `designsystem` | 2026-11-06 | `2b8a290b` | Material Symbols drawables no code drew. |
| `PanelSearchField`, `SearchHitRow`, `PanelTokens.searchHitHeight`, `PanelTokens.searchHitGap` | `designsystem` | 2026-11-07 | `2b8a290b` | A search field in the control panel and a row for a message it found; Mail's inbox search sheet, their only caller, went with the one search UI. |
| `FilterTokens.pickAvatarSize` | `designsystem` | 2026-11-07 | `2b8a290b` | The 36 dp avatar size of a filter page's people rows. |
| `Spacing.xxs`, `Spacing.m`, `Spacing.xxl` | `designsystem` | 2026-11-07 | `6e4baf53` on `refactor/untangle` | The 2, 8, and 24 dp steps of the spacing scale, which no token used. |
| `ComposeTokens.fromAvatarSize`, `ComposeTokens.fromLabelMaxWidth`, `ComposeTokens.dropdownAvatarSize` | `designsystem` | 2026-11-07 | `refactor/untangle` | The Send-from pill's avatar and label width and an address dropdown's avatar; `SenderTokens` and `AvatarTokens.menuSize` hold them for every screen that shows a sender. |
| `appearanceIndex`, `appIconIndex` | `settings` | 2026-11-07 | `d98e4a45` | Listed the Appearance page and the App icon row for settings search; `appearanceSections` and `appIconRow` declare them once for the page and search. |
| `AppIconSetting` | `settings` | 2026-11-07 | `d98e4a45` | Drew the App icon row and its sheet; `appIconRow` is the row as a `ChoiceRowSpec`. |
| `FolderGlyphKind`, `FolderGlyph(kind)`, `RowFolderIcon.kind` | `designsystem` | 2026-11-07 | `d98e4a45` | Asked a folder glyph for the inbox symbol; `icon = symbol_inbox` with no color draws the same. |
| `JmapFilter.and`, `JmapFilter.or` | `jmap` | 2026-11-07 | `d98e4a45` | Infix filter operators that kept chains of one operator flat; `allOf` and `anyOf` build the same filters. |
| `DateFormatPreference.fromStorageValue` | `dateformat` | 2026-11-07 | `d98e4a45` | Read a saved date format; `DateFormatSettingsState` reads it, and only a test called this. |
| `QuickPerson`, `QuickViewTokens.personStart`, `personEnd`, `personGap`, `personNameSize`, `personAddressSize` | `designsystem` | 2026-11-07 | `2b8a290b` | A quick view module for one person, such as who sent the open message; the Read view's expanded stop shows the message details instead. |
| `DetailsTable` | `designsystem` | 2026-11-07 | `2b8a290b` | Detail rows on a raised card; the Read view draws `DetailRows` straight on its panel at both stops. |

`ContactCardFilter` stays: the `avatar` module searches cards with it.
