# 0016: JMAP contacts modules

Status: written on Meta `feat/jmap-contacts` on 2026-10-22, not merged. No app uses the modules
yet, and their tests have not run.

## Decision

The user asked for an implementation of the JMAP contacts specifications with no UI. Otus Meta
adds two library modules and extends `jmap`:

- `com.otus:jscontact` holds JSContact cards (RFC 9553): one Kotlin class per object type,
  validation, and localization.
- `com.otus:jmap-contacts` holds JMAP for Contacts (RFC 9610): the AddressBook and ContactCard
  data types, `AddressBook/set` with its extra arguments, the ContactCard filter and sorts, and
  patch builders.
- `com.otus:jmap` gains `/copy`, `/queryChanges`, filter operators, a PatchObject builder,
  `UtcDateSerializer` for UTCDates, and the Principal and ShareNotification types of RFC 9670 in
  `com.otus.jmap.sharing`.

The architecture guide moves code here when the second app needs it. This is new code that
neither app calls yet, so it goes ahead of that rule because the user asked for it, as
[decision 0007](0007-shared-feature-modules.md) did for settings and accounts. Both apps plan to
use contacts: Mail for contact management and recipients
([Mail #8](https://github.com/reazndev/otus-mail/issues/8),
[Mail #6](https://github.com/reazndev/otus-mail/issues/6),
[Mail #19](https://github.com/reazndev/otus-mail/issues/19)), and Calendar for attendee
suggestions ([Calendar #7](https://github.com/reazndev/otus-calendar/issues/7)). See
[the contacts guide](../contacts.md) for the API.

## Two modules

RFC 9553 defines JSContact on its own, and RFC 9610 only carries it over JMAP. `jscontact`
depends on kotlinx.serialization alone, with no Ktor and no JMAP client, so code that reads or
writes cards does not pull in networking. `jmap-contacts` depends on `jmap` and `jscontact` and
holds everything that needs a server. This follows the architecture rule to split a module on a
real dependency difference.

RFC 9610 registers `id`, `addressBookIds`, and `blobId` as JSContact properties (section 7.5), so
`Card` and `Media` in `jscontact` carry them. A separate JMAP card class would repeat the 30
properties of RFC 9553 to add two.

## Sharing lives in jmap

RFC 9670 defines Principals and ShareNotifications for every data type that can be shared, not
only for contacts. AddressBooks use `shareWith` now, and shared calendars
([Calendar #9](https://github.com/reazndev/otus-calendar/issues/9)) will use the same Principals.
With the types in `jmap`, a later `jmap-calendars` module does not depend on `jmap-contacts`. The
`sharing` package holds only the wire types and the capability readers. Each data type keeps its
own rights in `shareWith`, such as `AddressBookRights`, because RFC 9670 leaves the rights to
each type.

## Paging stays in extra

`QueryMethod` and the new `QueryChangesMethod` take paging arguments such as `position`,
`anchor`, `anchorOffset`, `calculateTotal`, and `upToId` through `extra`, as `QueryMethod` did
before. Mail already pages this way in its inbox listing and its message cleaner. Typed
parameters would add a second way to send the same arguments through a public constructor that
Mail already calls. `QueryResult` now reports the `limit` the server applied, and
`JmapSession.maxObjectsInGet` the most objects one `/get` returns, which is what a caller needs to
size its pages. Revisit this when a second caller pages through contacts.

## Unknown properties stay in extra

RFC 9553 requires implementations to keep unknown and vendor-specific properties (sections 1.7.4
and 1.8.1). kotlinx.serialization can only drop unknown keys or fail on them. A client that drops
them and later writes a whole card, such as a `/copy` or a creation from a cached card, deletes
data that another client stored. So every JSContact class has a `@Transient extra` map, and one
serializer, `JsContactSerializer`, moves undeclared members into it when decoding and writes them
back when encoding. A declared property always wins over an `extra` entry, so encoding and
decoding agree.

Enumerated values follow the same rule. They are value classes with the registered values as
constants, not Kotlin enums, so a value registered later or a vendor value decodes and survives a
round trip.

Updates still go through `contactCardPatch`, which sends only the properties that change, so the
server keeps whatever this client did not decode.

## Limits

- No app uses the modules. Mail and the `avatar` module keep their own small contact types for
  recipient suggestions and contact photos.
- The tests are written from the RFC figures but have not run, and nothing has been tried against
  a server.
- There is no UI, storage, or sync of contacts.
- What each RFC leaves out of the implementation is listed in the conformance tables of
  [the contacts guide](../contacts.md) and [the JMAP guide](../jmap.md).

## Order

1. Library modules and this record. Written, tests not run.
2. Run the tests and `checkAll`, then test against a server that offers contacts.
3. Proposed: Mail moves its contact photo lookup and recipient suggestions to `jmap-contacts`.
4. Proposed: contact management in Mail and attendee suggestions in Calendar, as the issues above
   plan.
