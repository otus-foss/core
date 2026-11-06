# Removed APIs

Public APIs that no code in Mail or Calendar called, outside their own tests, were removed on
2026-11-06 (decision 3 of the untangle refactor). Each entry names the last commit that has it, on
`origin/main` of otus-meta. To bring one back, restore its files from that commit with
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

`ContactCardFilter` stays: the `avatar` module searches cards with it.
