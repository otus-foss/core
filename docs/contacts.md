# Contacts

`com.otus:jscontact` and `com.otus:jmap-contacts` implement JSContact (RFC 9553) and JMAP for
Contacts (RFC 9610). They have no UI, no storage, and no sync. See
[decision 0016](decisions/0016-contacts-modules.md).

No app uses these modules yet. Mail and the `avatar` module still define their own small
contact types for recipient suggestions and contact photos. The tests listed below are written
but have not run yet, and nothing has been tried against a server.

## Modules

| Module | Package | Depends on | Holds |
| --- | --- | --- | --- |
| `jscontact` | `com.otus.jscontact` | kotlinx.serialization | JSContact cards, validation, and localization |
| `jmap-contacts` | `com.otus.jmap.contacts` | `jmap`, `jscontact` | The AddressBook and ContactCard data types, filters, sorts, and patches |

Both modules use Kotlin explicit API mode and target Android and iOS. The parts of JMAP that other
data types need too live in `jmap`: `/queryChanges`, `/copy`, filter operators, PatchObjects,
`UtcDateSerializer`, and the sharing types of RFC 9670. See [JMAP](jmap.md).

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
- `Card.validate()` returns a `JsContactProblem` for each rule the card breaks. Each problem holds
  the JSON Pointer of the value at fault, such as `/emails/e1/pref`, and a message that names the
  section. An empty list means the card is valid.
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

### jmap-contacts

- `ContactCardType` is the `ContactCard` data type for the generic `GetMethod`, `ChangesMethod`,
  `QueryMethod`, `QueryChangesMethod`, `SetMethod`, and `CopyMethod`. Its objects are `Card`s.
- `Card.toCreation()` writes a card as one `create` entry of `ContactCard/set`, without the
  server-set `id`. It throws when `addressBookIds` names no book.
- `contactCardPatch { … }` builds the patch of one `ContactCard/set` update. It has a handle for
  each Card property. Every handle has `set` and `remove`. `Id[X]` maps such as `emails` add `put`
  and `remove` for one entry, and sets such as `keywords` and `addressBookIds` add `add` and
  `remove` for one key. `raw { … }` reaches paths the handles do not, such as one property inside
  an entry.
- `ContactCardFilter` holds the FilterCondition of RFC 9610, section 3.3.1. `toFilter()` turns it
  into a `JmapFilter`, which combines with other filters through `allOf`, `anyOf`, `noneOf`, `and`,
  and `or`.
- `ContactCardSort` builds the comparators of section 3.3.2: `created`, `updated`, `nameGiven`,
  `nameSurname`, and `nameSurname2`. The name sorts take an optional `collation`.
- `AddressBook` holds a book's properties, with `AddressBookRights` for `myRights` and for each
  `shareWith` entry. `AddressBookOrder` sorts books for display, and `AddressBookType` is the data
  type for `GetMethod` and `ChangesMethod`.
- `AddressBookSetMethod` sends `AddressBook/set` with `NewAddressBook` creations,
  `addressBookPatch { … }` updates, destructions, and the arguments `onDestroyRemoveContents` and
  `onSuccessSetIsDefault` (section 2.3).
- `JmapAccount.contacts` reads the account's `ContactsCapability`, with `maxAddressBooksPerCard`
  and `mayCreateAddressBook` (section 1.4.1). It is null when the account has no contacts.

A patch that adds a work email, drops a phone, and moves the card to another book:

```kotlin
val update = contactCardPatch {
    emails.put("e2", EmailAddress("joe@work.example.com"))
    phones.remove("p1")
    addressBookIds.add("b2")
    addressBookIds.remove("b1")
}
connection.call(SetMethod(ContactCardType, accountId, update = mapOf(cardId to update)))
```

## Behavior

### Unknown properties

RFC 9553 requires a client to keep properties it does not know (sections 1.7.4 and 1.8.1). Every
JSContact class has an `extra` map. Decoding puts each member the class does not declare into it,
and encoding writes those members back after the declared ones, so a card read from a server and
written again loses nothing. A declared property always wins over an `extra` entry of the same
name. Encoding writes `@type` first on every object. Decoding accepts a missing `@type` and fails
when a present one names another type.

These serializers work only with JSON. Any other kotlinx.serialization format throws.

### Partial cards

A `/get` with `properties` returns only some properties (RFC 8620, section 5.1). Every `Card`
property therefore decodes as optional, `uid` included, although RFC 9553 makes `uid` mandatory.
`validate()` reports what a complete card lacks, with one exception. RFC 9553 makes `version`
mandatory and gives it no default (section 2.1.2), but decoding fills in `1.0` when it is
missing, so `validate()` cannot report a missing version. Encoding always writes `version`, so a
partial card encodes with a property the server did not send. Send changes to an existing card
with `contactCardPatch`, never as a re-encoded partial card.

Properties inside an entry stay mandatory where the RFC says so. An `EmailAddress` without
`address`, a `Media` without `kind`, or a `Note` without `note` fails decoding, and one such card
fails the whole `ContactCard/get` result with `JmapException.NotJmap`. Values out of range, such as
a `pref` of 500 or month 13, still decode, and `validate()` reports them.

Over JMAP a `Media` may hold a `blobId` in place of its `uri` (RFC 9610, section 3). `Media.uri` is
then empty, and encoding leaves it out.

### Dates

`Card.created`, `Card.updated`, `Note.created`, and `Timestamp.utc` are UTCDateTime values
(RFC 9553, section 1.4.5) and decode to `kotlin.time.Instant`. Encoding writes the one form the
RFC allows: the offset `Z`, and a fraction of a second only when it is not zero, without trailing
zeros. Decoding accepts more, so one non-canonical date from a server does not make the card
unreadable:

- A numeric offset, such as `+02:00`, and a zero fraction, such as `10:10:10.000Z`, read as the
  same instant, and encoding writes it in the `Z` form.
- A leap second, such as `2016-12-31T23:59:60Z`, reads as `23:59:59.999999999Z`, because
  `Instant` counts no leap seconds.
- Decoding drops fraction digits after the ninth, because `Instant` holds nanoseconds at most.

Decoding rejects a year outside 0000 to 9999, a signed year, and a numeric offset that moves the
instant out of that range, because RFC 3339 writes a year as four digits (section 5.6). Every date
that decodes therefore encodes again. A rejected date fails the whole card, and over JMAP the
whole `ContactCard/get` with `JmapException.NotJmap`.

### Validation

`validate()` checks what decoding does not:

- mandatory properties, such as `uid`, `components` or `full` on a `Name`, and at least one of
  `components`, `coordinates`, `countryCode`, `full`, and `timeZone` on an `Address`
- values that depend on each other, such as `members` only on group cards, separators only in
  ordered components, `phonetic` only with a script or system on the parent, `sortAs` keys only for
  component kinds the name has, and a `Media` `blobId` only with a `mediaType`
- ranges: `pref` from 1 to 100, `listAs` above zero, and PartialDate months and days that exist,
  with Gregorian leap years
- syntax: Id keys, URIs, the shape of RFC 5646 language tags, `geo:` coordinates, ISO 3166-1
  alpha-2 country codes, script subtags, and the JMAP `id` and `addressBookIds`
- email addresses: an RFC 5322 addr-spec (section 3.4.1) without comments or obsolete forms,
  with non-ASCII text as RFC 6532 allows
- the version: only major version 1, such as `1.0` or `1.10`, is valid, because RFC 9553
  registers no other (sections 1.9 and 2.1.2)
- sets whose values must all be `true`, such as `keywords`, `members`, `Phone.features`, and
  Address `contexts`
- enumerated values that differ from a registered value only in case (section 1.7.1), and vendor
  values that are not in the `example.com:name` form (section 1.8.2)
- names in `extra` that are reserved, that differ from a declared property only in case, or that
  are neither in registered-name syntax nor vendor-specific
- each localization: its tag must be a language tag, its patch must apply, and the localized card
  must not add problems, which are reported at the tag

It does not check:

- time zone names, which need the IANA Time Zone Database
- language tags against the subtag registry
- phone numbers, which RFC 9553 allows as free text
- a missing `version`, because decoding fills in `1.0` (see [Partial cards](#partial-cards))
- that `uid` is unique in an account, or that `members` and `relatedTo` keys name existing cards
- `maxAddressBooksPerCard`, which a caller reads from `JmapAccount.contacts` before adding a book
  to a card

Unknown values and unknown property names in a valid syntax pass, as sections 1.7.4 and 1.9
require. Validation never changes the card.

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
`validate()` applies each localization and reports such problems at the localization's language
tag.

`JsContactPatch.apply` follows section 1.4.3. Keys are JSON Pointers without the leading `/`, null
removes a member, and an existing array item can be replaced. When any key is invalid or two keys
overlap, it throws and applies nothing.

### JMAP patches

A JMAP PatchObject (RFC 8620, section 5.3) cannot point into an array, unlike a JSContact one.
`contactCardPatch` and `addressBookPatch` build JMAP patches through `jmapPatch`:

- Each path segment escapes `~` and `/`, so a free-form key such as an email Id or a Principal id
  stays one segment.
- Overlapping paths, such as `emails` and `emails/e1` in one patch, throw before anything is sent.
  A later value for the same path replaces the earlier one.
- Changing one entry needs the map on the server. When the card or book lacks the map, set the
  whole map.
- `Name.components` is an array, so a name change sets the whole `name`.
- A card must stay in at least one book, so a move adds the new book and removes the old one in
  the same patch.
- Values are written exactly as a whole `Card` writes them, with `@type` and canonical UTCDateTime.

### Address books

- `AddressBookOrder` sorts by `sortOrder`, then by name ignoring case, then by name, then by id.
  Common Kotlin has no locale-aware collation.
- `NewAddressBook` always sends `isSubscribed`, `true` by default, because RFC 9610 gives it no
  default. A name must have 1 to 255 octets of UTF-8 and `sortOrder` must be from 0 to 2^31 - 1.
  Other values throw before anything is sent, in `NewAddressBook` and in `addressBookPatch`.
- `AddressBookRights` always encodes all four rights, because RFC 9670 does not say what a missing
  right means.
- Destroying a book that still holds cards fails with `addressBookHasContents`, unless
  `onDestroyRemoveContents` is set. The server then takes the cards out of the book and destroys
  the ones in no other book. The response lists only books, so sync ContactCards afterwards.
- The server may ignore `onSuccessSetIsDefault` without an error. Read `isDefault` in the result
  before assuming the change.
- `share`, `unshare`, `grant`, and `revoke` patch one Principal's entry in `shareWith` and leave
  the others. `shareWith(map)` replaces the whole map.

### Capabilities

- The client refuses every contacts method with `JmapException.Unsupported` when the session or
  the account lacks `urn:ietf:params:jmap:contacts`, so nothing reaches the server. A `CopyMethod`
  needs the capability in both accounts.
- `JmapAccount.contacts` reads a malformed capability object as the cautious defaults: no known
  limit, and `mayCreateAddressBook` false.

### The RFC 9610 examples

Figure 4 of RFC 9610 puts `oldState` and `newState` inside `updated`, where RFC 8620, section 5.3,
puts them at the top level. The client rejects that response as `NotJmap`, because it lacks a
top-level `newState`. `ContactsExamplesTest` checks this and also reads a corrected copy.

## RFC 9553 conformance

| Section | Behavior | Tests |
| --- | --- | --- |
| 1.3 | Each object writes its `@type` first; a present `@type` that names another type fails decoding | `JsContactSerializerTest`, `CardIdentityFiguresTest` |
| 1.4.1 | `Id[X]` maps keep their keys; `validate` checks the Id syntax | `CardValidationValuesTest` |
| 1.4.3 | `JsContactPatch.apply`: escapes, array replacement, existing parents, no overlapping keys, all or nothing | `JsContactPatchTest` |
| 1.4.4 | Resources are `Calendar`, `CryptoKey`, `Directory`, `Link`, or `Media`; `@type` `Resource` fails | `CardResourcesTest`, `CardCalendarsTest`, `CardMediaTest` |
| 1.4.5 | UTCDateTime written in canonical form; numeric offsets, zero fractions, leap seconds, and fractions longer than nine digits read; years outside 0000 to 9999 rejected | `UtcDateTimeSerializerTest`, `CardNotesTest` |
| 1.5.1 | Open contexts; `billing` and `delivery` only on addresses | `CardValidationValuesTest`, `CardAddressTest` |
| 1.5.3 | `pref` from 1 to 100 | `CardValidationValuesTest` |
| 1.5.4 | A `phonetic` value needs a script or system on its parent | `CardValidationObjectsTest`, `NameFiguresTest` |
| 1.7.1 | Values that differ from a registered one only in case are invalid | `CardValidationValuesTest` |
| 1.7.2, 1.7.3, 1.8.1 | Names of unknown properties: registered syntax or vendor-specific, never reserved | `CardValidationTest` |
| 1.7.4, 1.8.1 | Unknown and vendor-specific properties kept at every level | `CardTest`, `JsContactSerializerTest`, each figure test |
| 1.8.2 | Vendor values kept, non-ASCII ones included; malformed vendor values reported | `CardValidationValuesTest` |
| 1.9 | Any version decodes, and a missing one reads as `1.0`; only major version 1 validates | `CardValidationTest`, `CardTest` |
| 2.1 | Metadata: `uid` required, `members` only on groups, `prodId` not empty, `relatedTo` | `CardValidationTest`, `CardIdentityFiguresTest` |
| 2.2 | `name`, `nicknames`, `organizations`, `speakToAs`, `titles` | `NameFiguresTest`, `CardIdentityFiguresTest`, `CardValidationObjectsTest` |
| 2.3 | `emails`, `onlineServices`, `phones`, `preferredLanguages`; email addresses checked as addr-specs | `CardContactsTest`, `CardValidationValuesTest` |
| 2.4 | `calendars`, `schedulingAddresses`; their `uri` checked as a URI | `CardCalendarsTest`, `CardValidationValuesTest` |
| 2.5 | `addresses`, from figures 31 to 33 | `CardAddressTest`, `CardValidationObjectsTest` |
| 2.6 | `cryptoKeys`, `directories`, `links`, `media` | `CardResourcesTest`, `CardMediaTest` |
| 2.7.1 | `Card.localized`, which refuses a patch that removes `@type` or `version`; validation applies each localization | `CardLocalizationTest`, `CardValidationTest` |
| 2.8 | `anniversaries` with PartialDate as the default type, `keywords`, `notes`, `personalInfo` | `CardAnniversariesTest`, `CardNotesTest`, `CardValidationObjectsTest` |
| 2 | One card with every property, from the section 2 figures, round-trips and validates | `CardTest` |
| 3.7 | Companion constants match the IANA registry for six enumerations: Card kinds, relation types, name component kinds, phonetic systems, grammatical genders, title kinds | `CardNamesRegistryTest` |

vCard conversion (RFC 9555) covers the properties listed under [vCard](#vcard), and its tests are
`VCardTest` and `VCardWriteTest`. The checks `validate()` skips are listed under
[Validation](#validation).

## RFC 9610 conformance

| Section | Behavior | Tests |
| --- | --- | --- |
| 1.4.1 | Account capability with `maxAddressBooksPerCard` and `mayCreateAddressBook`; a malformed object reads as the defaults | `ContactsSupportTest` |
| 2 | AddressBook properties, rights, and display order | `ContactsExamplesTest`, `ContactsSupportTest` |
| 2.1 | `AddressBook/get`, figures 1 and 2 | `ContactsExamplesTest` |
| 2.2, 3.2 | `/changes` through the generic `ChangesMethod`; only the capability check is tested here | `ContactsSupportTest` |
| 2.3 | `AddressBook/set` with `onDestroyRemoveContents`, `onSuccessSetIsDefault`, `addressBookHasContents`, name and `sortOrder` limits, and `shareWith` patches | `AddressBookSetTest`, `AddressBookPatchTest`, `ContactsExamplesTest` |
| 3, 7.5 | A ContactCard is a `Card` with `id` and `addressBookIds`; `Media.blobId` stands in for `uri` | `CardTest`, `CardMediaTest`, `CardValidationTest` |
| 3.1 | `ContactCard/get` with `properties` returns partial cards | `ContactCardSyncTest` |
| 3.3, 3.3.1, 3.3.2 | `ContactCard/query` with every filter condition and sort property | `ContactCardFilterTest`, `ContactCardSyncTest` |
| 3.4 | `ContactCard/queryChanges` | `ContactCardSyncTest` |
| 3.5 | `ContactCard/set` creates from `toCreation` and updates through `contactCardPatch` | `ContactCardSyncTest`, `ContactCardPatchTest`, `ContactCardPatchHandlesTest` |
| 3.6 | `ContactCard/copy` from a shared account, with `onSuccessDestroyOriginal` | `ContactCardSyncTest` |
| 4 | Figures 1 to 4; figure 4 is rejected as written | `ContactsExamplesTest` |

Not implemented: a typed `ContactCard/set` method (use `SetMethod`), a check of
`maxAddressBooksPerCard` or `myRights` before sending, uploading a new photo (use
`JmapConnection.upload` and put its blob id in a `Media`), and locale-aware book order. The server
decides how text filters match.

## Checks

`./gradlew :jscontact:testAndroidHostTest :jmap-contacts:testAndroidHostTest` runs the tests in
the tables, and `checkAll` includes both modules. None of these tests have run yet, so every row
above is written, not passed. The tests use the RFC figures. `CardOfAllFigures` builds one card
with every property from the figures of RFC 9553, section 2, and `Rfc9610Figures` holds figures 1
to 4 of RFC 9610. The iOS tests need macOS, and no test runs against a real server.
