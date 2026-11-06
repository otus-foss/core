# Contacts

`com.otus:jscontact` and `com.otus:jmap-contacts` implement JSContact (RFC 9553) and JMAP for
Contacts (RFC 9610). They have no UI, no storage, and no sync. See
[decision 0016](decisions/0016-contacts-modules.md).

Mail uses both modules for its contacts feature, and the `avatar` module reads contact photos and
avatar styles through the same `Card` and `AddressBook` types. App storage, editing state, and
synchronization remain outside these libraries.

## Modules

| Module | Package | Depends on | Holds |
| --- | --- | --- | --- |
| `jscontact` | `com.otus.jscontact` | `json-types` | JSContact cards, vCard conversion, and localization |
| `jmap-contacts` | `com.otus.jmap.contacts` | `jmap`, `jscontact` | The AddressBook and ContactCard data types, the card filter, and card patches |

Both modules use Kotlin explicit API mode and target Android and iOS. The parts of JMAP that other
data types need too live in `jmap`: `/queryChanges`, filter operators, and PatchObjects. The
UTCDate serializer and the PatchObject path rules that JSContact shares with JMAP live in
`json-types`, which both `jmap` and `jscontact` use, so `jscontact` stays free of networking. See
[JMAP](jmap.md).

Within `jscontact`, public models and entry points stay in `com.otus.jscontact`. The `vcard`
subpackage holds internal parsing and conversion code. Callers use `VCard.parse()`, `VCard.write()`,
and `isEmailAddress` from the root package.

## API

### jscontact

- `Card` is one JSContact Card with every property of RFC 9553, section 2, plus the JMAP
  properties `id` and `addressBookIds` that RFC 9610 registers (section 7.5). Read and write it
  with `JsContactJson` and `Card.serializer()`.
- Each object type has its own class: `Name` and `NameComponent`, `Nickname`, `Organization` and
  `OrgUnit`, `SpeakToAs` and `Pronouns`, `Title`, `EmailAddress`, `OnlineService`, `Phone`,
  `LanguagePref`, `Calendar`, `SchedulingAddress`, `Address` and `AddressComponent`, `CryptoKey`,
  `Directory`, `Link`, `Media`, `Anniversary`, `Note` and `Author`, `PersonalInfo`, and
  `Relation`. `Calendar`, `CryptoKey`, `Directory`, `Link`, and `Media` implement
  `ContactResource` (section 1.4.4).
- `Anniversary.date` is an `AnniversaryDate`, either a `PartialDate` or a `Timestamp`
  (section 2.8.1).
- `Card.localized(language)` returns the card with the localization for that language tag
  applied (section 2.7.1). [Localizations](#localizations) lists the patches it refuses.
- `JsContactPatch.apply(target, patch)` applies a JSContact PatchObject to a JSON object
  (section 1.4.3).

#### vCard

`VCard.parse(text)` reads a vCard file and returns a `VCardImport` with the cards it read and a
`VCardFailure` for each entry it skipped. `VCard.write(cards)` writes vCard 4.0. Both map
properties as RFC 9555 does.

- Parsing reads vCard 3.0 and 4.0, and the quoted-printable values of vCard 2.1, from a file with
  any number of entries. It unfolds lines, ignores the case of names and parameter keys, reads
  `group.` prefixes, `TYPE` lists in any of their forms, `PREF`, backslash escapes, and RFC 6868
  caret escapes.
- It maps FN, N, NICKNAME, EMAIL, TEL, ORG, TITLE, ROLE, BDAY, ANNIVERSARY, DEATHDATE, NOTE, ADR,
  UID, KIND, MEMBER, PHOTO, and URL. It also reads Apple's `X-ABLabel` labels, its
  `X-APPLE-OMIT-YEAR` birthdays, and its vCard 3.0 group properties.
- An entry becomes a card when it has any mapped property besides KIND and UID. Any other entry,
  or one without `END:VCARD`, becomes a failure with its 1-based position and a reason such as "It
  has no contact details." A card without FN gets its full name from N, and one without KIND is an
  individual. An FN marked `DERIVED=TRUE` (RFC 9554) is not read as a name. Map keys are `e1`,
  `p1`, and so on, in file order. A byte-order mark at the start of the file is skipped.
- Writing uses CRLF, folds lines at 75 octets, escapes text, and writes labels as `X-ABLabel` in a
  group. A card without a name gets its organization, email address, or phone number as FN, marked
  `DERIVED=TRUE`. `VCard.parse` reads the output back to the same card.

Limits:

- Other properties, such as IMPP, GEO, CATEGORIES, LOGO, and RELATED, are ignored, so a card read
  and written again loses them. JSContact properties outside the list above are not written.
- ADR reads and writes the seven RFC 6350 components. Writing puts room, floor, and building into
  the extended address and the other street kinds into the street, so they read back as
  `apartment` and `name`.
- Quoted-printable text decodes as UTF-8 or ISO-8859-1, and other charsets decode as UTF-8.
  Folded base64 data of vCard 2.1, which has no leading space, is not read.
- A photo that has only a JMAP `blobId` is not written. Dates in free text, such as
  `BDAY;VALUE=text:circa 1800`, are skipped, and a date with a time reads as a date unless it is
  in UTC.
- A line break can never start a new content line (RFC 6350, section 3.2). Text escapes a CR as a
  line break. A MEMBER, PHOTO, or URL value percent-encodes its control characters, such as CR and LF
  as `%0D` and `%0A`, so it reads back with them encoded. A `kind` that is not a vCard token, such as
  the vendor kind `example.com:baz`, is not written and reads back as an individual.

### jmap-contacts

- `ContactCardType` is the `ContactCard` data type for the generic `GetMethod`, `ChangesMethod`,
  `QueryMethod`, `QueryChangesMethod`, and `SetMethod`. Its objects are `Card`s.
- `Card.toCreation()` writes a card as one `create` entry of `ContactCard/set`, without the
  server-set `id`. It throws when `addressBookIds` names no book.
- `cardPatch(before, after)` builds the patch of one `ContactCard/set` update: it sets each
  top-level property that differs and removes the ones `after` dropped. Whole properties keep the
  patch valid on any server, since a path into a property needs its parent there.
- `ContactCardFilter` holds the FilterCondition of RFC 9610, section 3.3.1. `toFilter()` turns it
  into a `JmapFilter`, which combines with other filters through `allOf`, `anyOf`, `noneOf`, `and`,
  and `or`.
- `AddressBook` holds a book's properties, with `AddressBookRights` for `myRights` and for each
  `shareWith` entry. `AddressBookOrder` sorts books for display, and `AddressBookType` is the data
  type for `GetMethod` and `ChangesMethod`.
A patch that moves a card to another book:

```kotlin
val moved = card.copy(addressBookIds = mapOf("b2" to true))
connection.call(SetMethod(ContactCardType, accountId, update = mapOf(cardId to cardPatch(card, moved))))
```

## Behavior

### Unknown properties

RFC 9553 requires a client to keep properties it does not know (sections 1.7.4 and 1.8.1). Every
JSContact class has an `extra` map. Decoding puts each member the class does not declare into it,
and encoding writes those members back after the declared ones, so a card read from a server and
written again loses nothing. A declared property always wins over an `extra` entry of the same
name. Encoding writes `@type` first on every object. Decoding accepts a missing `@type` and fails
when a present one names another type.

These serializers work only with JSON. Any other kotlinx.serialization format throws. Decoding
also throws a `SerializationException` for an object with a member that nests arrays or objects
more than 64 levels deep, such as a hostile vendor property, because encoding recurses once per
level and would overflow the stack when the card is written again.

### Partial cards

A `/get` with `properties` returns only some properties (RFC 8620, section 5.1). Every `Card`
property therefore decodes as optional, `uid` included, although RFC 9553 makes `uid` mandatory.
RFC 9553 makes `version` mandatory and gives it no default (section 2.1.2), but decoding fills in
`1.0` when it is missing. Encoding always writes `version`, so a partial card encodes with a
property the server did not send. Send changes to an existing card with `cardPatch` over two cards
read with the same properties, never as a re-encoded partial card.

Properties inside an entry stay mandatory where the RFC says so. An `EmailAddress` without
`address`, a `Media` without `kind`, or a `Note` without `note` fails decoding, and one such card
fails the whole `ContactCard/get` result with `JmapException.NotJmap`. Values out of range, such as
a `pref` of 500 or month 13, still decode; nothing checks them on the device.

Over JMAP a `Media` may hold a `blobId` in place of its `uri` (RFC 9610, section 3). `Media.uri` is
then empty, and encoding leaves it out.

### Dates

`Card.created`, `Card.updated`, `Note.created`, and `Timestamp.utc` are UTCDateTime values
(RFC 9553, section 1.4.5) and decode to `kotlin.time.Instant` through `json-types`'
`UtcDateSerializer`, the one JMAP uses for UTCDates. Encoding writes the one form the
RFC allows: the offset `Z`, and a fraction of a second only when it is not zero, without trailing
zeros. Decoding accepts more, so one non-canonical date from a server does not make the card
unreadable:

- A numeric offset, such as `+02:00`, and a zero fraction, such as `10:10:10.000Z`, read as the
  same instant, and encoding writes it in the `Z` form.
- A leap second, such as `2016-12-31T23:59:60Z`, reads as `23:59:59.999999999Z`, because
  `Instant` counts no leap seconds. A seconds field of three digits, such as `:600`, is malformed.
- Decoding drops fraction digits after the ninth, because `Instant` holds nanoseconds at most.

Decoding rejects a year outside 0000 to 9999, a signed year, and a numeric offset that moves the
instant out of that range, because RFC 3339 writes a year as four digits (section 5.6). Every date
that decodes therefore encodes again. A rejected date fails the whole card, and over JMAP the
whole `ContactCard/get` with `JmapException.NotJmap`.

### Validation

`Card.validate()` and the `validation` package were removed on 2026-11-06 because no app called
them; see [removed APIs](removed-apis.md). Decoding still enforces the JSON types and mandatory
properties inside entries, and `isEmailAddress` still checks an RFC 5322 addr-spec.

### Open enumerations

Each enumerated property is a value class, such as `CardKind`, `ContactContext`, `PhoneFeature`,
or `MediaKind`, with the registered values as companion constants (RFC 9553, section 3.7). Any
string decodes and encodes unchanged, so a value registered later or a vendor value such as
`example.com:pager` survives a round trip. A `when` over these values needs an `else` branch.
Values are case-sensitive (section 1.7.1).

### Localizations

`Card.localized(language)` copies the card without `localizations`, applies the PatchObject for
that tag, and sets the copy's `language`. Tags match ignoring case, and an exact match wins. A card
without a localization for the tag comes back unchanged, and `de-AT` does not fall back to `de`.

`localized()` throws `IllegalArgumentException` and applies nothing when the patch:

- breaks a rule of section 1.4.3 that `JsContactPatch.apply` enforces, such as a missing parent
- patches `localizations` (section 2.7.1)
- removes `@type` or `version` with null, which decoding would fill in again unnoticed
- sets a value of the wrong JSON type, such as a number for `name`, so the result does not decode

Rule 4 of section 1.4.3 also forbids values that decode but break their property, such as a
`pref` of 0, a key that is not an Id, or a null that removes `uid`. `localized()` applies those.

`JsContactPatch.apply` follows section 1.4.3. Keys are JSON Pointers without the leading `/`, null
removes a member, and an existing array item can be replaced. When any key is invalid or two keys
overlap, it throws and applies nothing. It reads keys with the same `json-types` rules that
`jmapPatch` writes them with.

### JMAP patches

A JMAP PatchObject (RFC 8620, section 5.3) cannot point into an array, unlike a JSContact one.
`cardPatch` therefore sets whole top-level properties:

- A changed `emails`, `name`, or `addressBookIds` goes as the whole value, so the patch is valid
  on a server whose card lacks the parent of a deeper path.
- A card must stay in at least one book, so a move sets `addressBookIds` with the new book.
- Values are written exactly as a whole `Card` writes them, with `@type` and canonical UTCDateTime.

### Address books

- `AddressBookOrder` sorts by `sortOrder`, then by name ignoring case, then by name, then by id.
  Common Kotlin has no locale-aware collation.
- `AddressBookRights` always encodes all four rights, because RFC 9670 does not say what a missing
  right means.
- Writing address books (`AddressBook/set`) was removed on 2026-11-06 because no app called it;
  see [removed APIs](removed-apis.md).

### Capabilities

- The client refuses every contacts method with `JmapException.Unsupported` when the session or
  the account lacks `urn:ietf:params:jmap:contacts`, so nothing reaches the server.
- `JmapConnections.account(loginId, JmapCapability.CONTACTS)` finds the account that holds a
  login's contacts.

### The RFC 9610 examples

Figure 4 of RFC 9610 puts `oldState` and `newState` inside `updated`, where RFC 8620, section 5.3,
puts them at the top level. Figures 3 and 4 change the default book, which the library no longer
sends, so the tests use figures 1 and 2.

## RFC 9553 conformance

| Section | Behavior | Tests |
| --- | --- | --- |
| 1.3 | Each object writes its `@type` first; a present `@type` that names another type fails decoding | `JsContactSerializerTest`, `CardIdentityFiguresTest` |
| 1.4.1 | `Id[X]` maps keep their keys | `CardTest`, each figure test |
| 1.4.3 | `JsContactPatch.apply`: escapes, array replacement, existing parents, no overlapping keys, all or nothing | `JsContactPatchTest` |
| 1.4.4 | Resources are `Calendar`, `CryptoKey`, `Directory`, `Link`, or `Media`; `@type` `Resource` fails | `CardResourcesTest`, `CardCalendarsTest`, `CardMediaTest` |
| 1.4.5 | UTCDateTime written in canonical form; numeric offsets, zero fractions, leap seconds, and fractions longer than nine digits read; years outside 0000 to 9999 rejected | `UtcDateSerializerTest` (json-types), `CardNotesTest` |
| 1.5.1 | Open contexts | `CardAddressTest` |
| 1.7.4, 1.8.1 | Unknown and vendor-specific properties kept at every level | `CardTest`, `JsContactSerializerTest`, each figure test |
| 1.8.2 | Vendor values kept, non-ASCII ones included | `JsContactSerializerTest` |
| 1.9 | Any version decodes, and a missing one reads as `1.0` | `CardTest` |
| 2.1 | Metadata: `uid`, `members`, `prodId`, `relatedTo` | `CardIdentityFiguresTest` |
| 2.2 | `name`, `nicknames`, `organizations`, `speakToAs`, `titles` | `NameFiguresTest`, `CardIdentityFiguresTest` |
| 2.3 | `emails`, `onlineServices`, `phones`, `preferredLanguages` | `CardContactsTest` |
| 2.4 | `calendars`, `schedulingAddresses` | `CardCalendarsTest` |
| 2.5 | `addresses`, from figures 31 to 33 | `CardAddressTest` |
| 2.6 | `cryptoKeys`, `directories`, `links`, `media` | `CardResourcesTest`, `CardMediaTest` |
| 2.7.1 | `Card.localized`, which refuses a patch that removes `@type` or `version` | `CardLocalizationTest` |
| 2.8 | `anniversaries` with PartialDate as the default type, `keywords`, `notes`, `personalInfo` | `CardAnniversariesTest`, `CardNotesTest` |
| 2 | One card with every property, from the section 2 figures, round-trips | `CardTest` |
| 3.7 | Companion constants match the IANA registry for six enumerations: Card kinds, relation types, name component kinds, phonetic systems, grammatical genders, title kinds | `CardNamesRegistryTest` |

vCard conversion (RFC 9555) covers the properties listed under [vCard](#vcard), and its tests are
`VCardTest` and `VCardWriteTest`.

## RFC 9610 conformance

| Section | Behavior | Tests |
| --- | --- | --- |
| 2 | AddressBook properties, rights, and display order | `ContactsExamplesTest`, `ContactsSupportTest` |
| 2.1 | `AddressBook/get`, figures 1 and 2 | `ContactsExamplesTest` |
| 2.2, 3.2 | `/changes` through the generic `ChangesMethod`; only the capability check is tested here | `ContactsSupportTest` |
| 3, 7.5 | A ContactCard is a `Card` with `id` and `addressBookIds`; `Media.blobId` stands in for `uri` | `CardTest`, `CardMediaTest` |
| 3.1 | `ContactCard/get` with `properties` returns partial cards | `ContactCardSyncTest` |
| 3.3, 3.3.1 | `ContactCard/query` with every filter condition | `ContactCardFilterTest`, `ContactCardSyncTest` |
| 3.4 | `ContactCard/queryChanges` | `ContactCardSyncTest` |
| 3.5 | `ContactCard/set` creates from `toCreation` and updates through `cardPatch` | `ContactCardSyncTest` |
| 4 | Figures 1 and 2 | `ContactsExamplesTest` |

Not implemented: `AddressBook/set`, `ContactCard/copy`, sort comparators (pass a `JmapSort`),
validation of a whole card, a typed `ContactCard/set` method (use `SetMethod`), a check of
`maxAddressBooksPerCard` or `myRights` before sending, uploading a new photo (use
`JmapConnection.upload` and put its blob id in a `Media`), and locale-aware book order. The server
decides how text filters match.

## Checks

`./gradlew :jscontact:testAndroidHostTest :jmap-contacts:testAndroidHostTest` runs the tests in
the tables, and `checkAll` includes both modules. The tests use the RFC figures. `CardOfAllFigures` builds one card
with every property from the figures of RFC 9553, section 2, and `Rfc9610Figures` holds figures 1
and 2 of RFC 9610. The iOS tests need macOS, and no test runs against a real server.

2026-11-06: the contact types now have one model. Avatar reads cards as `Card`, Mail's own card
types and its patch builder moved here as `cardPatch`, and the APIs no app called went; see
[removed APIs](removed-apis.md).
