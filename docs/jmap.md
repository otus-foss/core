# JMAP

The `jmap` module (`com.otus.jmap`, artifact `com.otus:jmap`) is a provider-independent client for
RFC 8620 (core), with the sharing types of RFC 9670 and login storage. It has no UI and no app
code, so Otus Mail and Otus Calendar can both use it. Mail-specific types live in the Mail app.
Developed and checked against Stalwart. It moved here from otus-mail at version 0.1.0.

## Module rules

- Kotlin explicit API mode: every public declaration is deliberate. Depends only on Ktor client
  core, kotlinx.serialization, and coroutines. Apps supply the HTTP engine (OkHttp on Android, Darwin
  on iOS).
- The architecture check enforces: package root `com.otus.jmap`; no Compose or lifecycle; no
  references to `com.otus.mail` or `com.otus.calendar`; no platform APIs in common code. File length,
  suppression, wildcard-import, detekt, and ktlint rules apply as everywhere else.
- Apps use it as `api(libs.otus.jmap)` from their `shared` module; see
  [Using the libraries from an app](consuming.md).

## API

- `JmapClient(engine, policy)`: one per process. `session(url, auth, trustedOrigins)` discovers the
  session; `request { … }`, `send { … }`, and calls on a session run method calls.
- `JmapConnection`: one signed-in server. Caches the session, refetches it when a response reports
  a different `sessionState` (RFC 8620, section 3.4), and drops it after a transport failure.
  Upload and download failures also invalidate the session for the next attempt. Failed requests
  are never automatically replayed, and a late failure cannot discard a newer cached session.
  `call(method)` for one call, `request { …; call(x) }` returning the last call's typed result, and
  `send { … }` for several results.
- `JmapConnections(client, logins)`: one connection per saved login; `forget(id)` after it changes.
  Credential loading and cache insertion share the invalidation lock, including concurrent first access.
- `JmapResponse[call]` decodes the explicit response and validates its method name.
  `responses(call)` exposes every response in wire order, including implicit calls and errors;
  an implicit failure does not replace the explicit method's result.
- Typed methods for any data type (RFC 8620, section 5): `GetMethod`, `QueryMethod`, `ChangesMethod`,
  `QueryChangesMethod`, `SetMethod`, and `CopyMethod`, with `JmapType(name, capability, serializer)`.
  Result references (section 3.7) chain calls in one request:
  `JmapIds.Result(query.resultOf("/ids"))`. Subclass `JmapMethod` for anything else.
- `QueryChangesMethod(type, accountId, sinceQueryState, filter, sort, maxChanges, extra)` updates a
  cached `/query` result (section 5.6). Pass the filter and sort of the query that returned the
  state. `QueryChangesResult` holds the `removed` ids and the `added` `AddedItem`s in index order.
  Splice the removed ids out, then the added ids in, lowest index first. `extra` carries `upToId`
  and `calculateTotal`. `cannotCalculateChanges` and `tooManyChanges` arrive as `MethodError`. The
  RFC's own example answers `"added": null`, which reads as an empty list.
- `CopyMethod(type, fromAccountId, accountId, create, onSuccessDestroyOriginal, ifFromInState,
  ifInState)` copies objects into another account (section 5.4), the only way to move them between
  accounts. Each `create` entry names its source object's `id`. `CopyResult` keeps each copy's
  outcome, and `createdId` gives the copy's new id. With `onSuccessDestroyOriginal`, the server
  destroys the originals through an implicit `/set` in the from account. Read that response from
  `JmapResponse.responses(call)` and decode it with a `SetMethod` that destroys the source ids.
- `JmapFilter` is the `filter` of a `/query` or `/queryChanges` (section 5.5): a type-specific
  `JmapFilter.Condition`, or a `JmapFilter.Operator` that combines filters with `AND`, `OR`, or
  `NOT`. `allOf`, `anyOf`, `noneOf`, `and`, and `or` build operators, and a chain such as
  `a and b and c` stays one flat `AND`. Send `toJson()`. A condition with an `operator` property
  throws.
- `jmapPatch { set(value, "keywords", "chopin"); remove("name") }` builds a `/set` PatchObject
  (section 5.3). Each path segment escapes `~` and `/` as JSON Pointer does, and `patchPath` writes
  one such key. Two paths where one is a prefix of the other throw, because the server would answer
  `invalidPatch`. `include` adds the entries of another patch.
- `JmapSort` takes a `collation`, an RFC 4790 algorithm from the core capability's
  `collationAlgorithms`. It is sent only when set.
- `QueryResult.canCalculateChanges` says whether `/queryChanges` can follow the query.
  `QueryResult.limit` holds the limit the server applied when it set or changed one, and callers
  page by that size. Paging arguments such as `position`, `anchor`, and `calculateTotal` go in
  `extra`, as [decision 0016](decisions/0016-contacts-modules.md) explains.
- `JmapSession.maxObjectsInGet` reads the most objects one `/get` returns (section 2). It is null
  when the server does not say or sends a malformed value.
- `UtcDateSerializer` reads and writes a UTCDate (section 1.4) as a `kotlin.time.Instant`. It
  writes the `Z` form with a fraction of a second only when it is not zero, without trailing
  zeros, such as `2014-10-30T06:12:00.5Z`. It reads any RFC 3339 offset. `Instant` holds neither a
  leap second nor more than nine fraction digits, so `23:59:60` reads as `23:59:59.999999999`, and
  reading drops the digits after the ninth. A year outside 0000 to 9999 fails in both directions,
  because RFC 3339 cannot write it. `jscontact` writes a UTCDateTime the same way, and the
  sharing types and `ContactCardFilter` use this serializer for their dates.
- Sharing (`com.otus.jmap.sharing`, RFC 9670): `PrincipalType` and `ShareNotificationType` are data
  types for the standard methods, under `JmapCapability.PRINCIPALS`. A `Principal` has an open
  `PrincipalKind`. Its `accounts` lists every account with that Principal's data that the user
  may access, including accounts the session leaves out because the user subscribes to nothing in
  them (sections 1.4 and 2). It is null when there are none. `PrincipalFilter` and
  `ShareNotificationFilter` build query conditions, and `ShareNotificationSort.created` is the one
  sort every server supports. RFC 9670 defines no sort for `Principal/query`. A ShareNotification
  can only be destroyed, which dismisses it. `JmapAccount.principals` reads
  `currentUserPrincipalId` (section 1.5.1), and `JmapAccount.principalOwner` reads the Principal
  that owns an account and the account that holds it (section 1.5.2). Both read a malformed value
  as null. Each shareable data type keeps its own `shareWith` rights, such as `AddressBookRights`
  in [`jmap-contacts`](contacts.md).
- A `Principal/get` or `ShareNotification/get` with `properties` returns only those properties and
  the id (RFC 8620, section 5.1). `Principal.type` and the `created`, `changedBy`, `objectType`,
  `objectAccountId`, and `objectId` of a `ShareNotification` are therefore nullable, although
  RFC 9670 requires them, and such a partial result decodes with null for the ones it leaves out.
  Other left-out properties take their defaults, such as an empty `name`.
- `SetMethod` sends creations, patches, and destructions as JSON, with optional `ifInState` and
  type-specific arguments. `SetResult` keeps each object's outcome; `SetError` carries `type`,
  `description`, `properties`, and `existingId`. Refer to an object created earlier in the same
  request as `#creationId`. A `stateMismatch` fails the whole call as `MethodError`.
  A response must contain `newState`, IDs for created objects, and outcomes for submitted literal
  IDs and creation references resolved within the same method. Additional server updates are
  allowed; conflicting success/error outcomes are rejected. References resolved in earlier
  methods remain server-validated because the decoder does not have that creation-ID map.
- Push (section 7): `PushSubscriptionSetMethod` creates, verifies, and destroys subscriptions, and
  `PushSubscriptionGetMethod` lists them. `NewPushSubscription` takes the push URL, which must be
  https, and the Web Push `PushKeys`. These calls have no account and no state, so the results'
  `state` and `newState` are empty. `PushMessage.decode` reads a push body into a `StateChange` or a
  `Verification`, and returns null for an unknown `@type`. `JmapSession.vapidPublicKey` reads the
  key a server advertises under RFC 9749. See [decision 0015](decisions/0015-push-notifications.md).
- `QueryResult.position` and `total` use `Long` and nullable `Long`, respectively, within
  RFC 8620's range of 0 through 2^53-1. Version 0.4.0 changes these from `Int` and nullable `Int`;
  consumers must rebuild and check bounds before converting counters to narrower types.
- `download(accountId, blobId, mediaType, maxBytes, name)` reads at most `maxBytes` of a blob through
  the session's `downloadUrl` (section 6.2). `name` fills the template's `{name}`, the file name the
  server reports in `Content-Disposition`, and defaults to `download`. Template values are
  percent-encoded, including slashes.
- `upload(accountId, bytes, mediaType)` stores a blob through the session's `uploadUrl` (section 6.1)
  with the same https and trusted-origin checks as downloads. Redirects are not followed. A blob over
  the advertised `maxSizeUpload` throws `UploadTooLarge` before anything is sent.
- `JmapSession.capabilityObjects` and `JmapAccount.capabilityObjects` keep each capability's object,
  such as the core limits (`maxObjectsInSet`, `maxSizeUpload`), the mail limits, or the supported Sieve
  extensions. Apps decode the parts they need; malformed values read as unknown.
- Before sending, the client checks every capability against the session and each account a call
  touches, both accounts for a `/copy`. It throws `Unsupported` instead of sending a request the
  server cannot serve.
- Logins (`com.otus.jmap.login`): `JmapLogin` identifies a server and user, not a JMAP account.
  Each app resolves its own account per capability with `JmapSession.accountFor`, so one login can
  serve mail and calendars. `LoginStore` keeps secrets apart from the list.

## Security model

| Threat | Mitigation |
| --- | --- |
| Credentials sent in clear | Every URL must be https: the session URL, each redirect, and `apiUrl`. `JmapPolicy.allowInsecureHttp` exists only for local test servers. |
| Credentials leaked by a redirect | Authorization headers go only to the origin the user entered and origins the user approved. Discovery can probe another origin without authorization, then raises `RedirectedElsewhere`; Mail asks the user and stores the approval. RFC 3986 resolution prevents the old query and URL credentials from being copied to the new host. The API and blob endpoints require a trusted origin before a request. |
| Hostile or broken responses | Responses are capped at 16 MiB. Envelope invocations are checked, and decoding failures become `NotJmap`. Custom decoders must use serializers, checked JSON accessors, or `require`; serialization, argument, cast, and missing-key errors are normalized. Cancellation and other programming errors propagate. |
| Secrets in logs or crash reports | `JmapAuth`, `LoginSecret`, and drafts print `***`. Secrets are not readable through public API; only headers are built from them. |
| Secrets at rest (Android) | AES-256-GCM with a non-exportable Android Keystore key; fresh IV per value; one synchronous commit per change; app backup disabled. |
| Secrets at rest (iOS) | Keychain, `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly`; replacement uses `SecItemUpdate`, and deletion errors preserve account metadata. |
| Unreadable stored data | A list that cannot be decoded is copied aside before it could be overwritten. An undecryptable secret is reported as `CredentialsUnavailable`, never as a server rejection. |

Not covered: certificate pinning, user-installed CAs and self-signed certificates (platform trust
only), OAuth, and sharing logins between the two installed apps.

## RFC 8620 conformance

| Section | Behavior | Tests |
| --- | --- | --- |
| 2, 2.2 | Session resource; `/.well-known/jmap` with redirects (relative `Location` included); RFC 3986 URL resolution | `JmapDiscoveryTest`, `JmapUrlResolutionTest` |
| 3.2–3.4 | Request envelope with `using`, invocations, `sessionState` | `JmapRequestTest`, `JmapConnectionTest` |
| 3.6.1 | Request-level problem details become `RequestFailed(type)` | `JmapDiscoveryTest` |
| 3.6.2 | Method-level errors become `MethodError(type)` for that call only | `JmapRequestTest` |
| 3.7 | Result references | `JmapRequestTest` |
| 5.1, 5.2, 5.5 | `/get`, `/changes`, `/query` | `JmapRequestTest`, Mail tests |
| 5.3 | `/set` creations, patches, destructions, `ifInState`, creation references, per-object errors and outcome validation | `JmapSetTest`, `JmapSetValidationTest` |
| 5.3 | PatchObjects with escaped JSON Pointer keys; overlapping paths rejected before sending | `JmapPatchTest` |
| 5.4 | `/copy` between two accounts, the capability checked in both, one outcome per copy, and the implicit destroy of the originals | `JmapCopyTest` |
| 5.5 | FilterOperator trees, Comparator `collation`, `canCalculateChanges`, and the server's `limit` | `JmapFilterTest`, `JmapQueryTest` |
| 5.6 | `/queryChanges` with the cached filter and sort, `maxChanges`, and `extra`; `added` in index order | `JmapQueryChangesTest` |
| 6.1 | Authenticated uploads through the advertised URI template, within `maxSizeUpload` | `JmapUploadTest` |
| 6.2 | Bounded authenticated blob downloads through the advertised URI template, with trusted-origin checks | `JmapDownloadTest` |
| RFC 7617 | Basic credentials; a colon in the username is rejected | `JmapLoginTest` |
| 1.3, 5.5 | Query counters support the full unsigned 53-bit range | `JmapDecodingTest` |
| 1.4 | UTCDate written in its `Z` form without trailing fraction zeros; any RFC 3339 offset, a leap second, and a fraction longer than nine digits read; years outside 0000 to 9999 refused | `UtcDateSerializerTest` |
| 2 | `maxObjectsInGet` from the core capability | `JmapSessionLimitsTest` |
| 7.1, 7.2 | `StateChange` and `PushVerification` messages; `PushSubscription/get` and `/set` without an account or state; https push URLs | `JmapPushTest` |
| RFC 9749 | The VAPID public key from the session | `JmapPushTest` |
| RFC 9670, 1.4, 2 | `Principal/get` and `/query`, an open Principal `type`, the `accounts` a user may access, and a partial `/get` without `type` | `PrincipalTest` |
| RFC 9670, 1.5 | The `principals` and `principals:owner` account capabilities | `SharingCapabilitiesTest` |
| RFC 9670, 2.4.1 | `Principal/query` conditions, alone or combined | `PrincipalTest` |
| RFC 9670, 3 | ShareNotifications, a partial `/get`, their filter with UTCDates, the sort by `created`, and dismissal by destroy | `ShareNotificationTest` |
| RFC 9670, 4 | `shareWith` patches on AddressBooks | `AddressBookPatchTest`, `AddressBookSetTest` in `jmap-contacts` |

`JmapPatchTest`, `JmapCopyTest`, `JmapFilterTest`, `JmapQueryTest`, `JmapQueryChangesTest`,
`UtcDateSerializerTest`, `JmapSessionLimitsTest`, and the three sharing tests are new on
`feat/jmap-contacts` and have not run yet.

Not implemented yet: `destroyFromIfInState` on `/copy`, `Blob/copy` (section 6.3), Blob/upload
(RFC 9404), the EventSource stream (section 7.3), and request limits such as
`maxCallsInRequest`. `maxObjectsInSet` and `maxObjectsInGet` are read but not enforced, and Mail
batches large moves within `maxObjectsInSet`. For RFC 9670, there is no typed `Principal/set`
(send a `SetMethod` with `PrincipalType`), decoding does not check a Principal's `email` or
`timeZone`, and only AddressBooks have typed `shareWith` rights.

## Use in the apps

Apps depend on `com.otus:jmap` and define their own data types with `JmapType` and the matching
capability, such as `MailboxType` in Mail or a `CalendarEvent` type in Calendar, and resolve the
account with `accountFor`. Each installed app passes its own storage names to `AndroidLoginStore`
and `IosLoginStore`, so apps never share logins. Mail keeps the names of its first release
(`otus_accounts` and `otus.account.credentials` on Android, `otus.accounts` and
`otus.mail.account` on iOS) so earlier accounts keep working.

## Checks

`./gradlew :jmap:testAndroidHostTest` runs protocol tests with Ktor's mock engine, concurrent
connection-cache tests, and Android preference-store tests with an injected cipher. It is required
by `checkAll` and the Android CI job. Tests cover explicit and implicit responses, method-name mismatches,
network failures and cancellation, capability checks, changes, account replacement, storage failures,
and corrupt-list preservation. Mail's app tests also cover storage-error recovery and same-named accounts.
`JmapBlobRecoveryTest` covers endpoint rediscovery, cancellation, and late failures after session replacement.

`./gradlew :jmap:iosSimulatorArm64Test` runs in the macOS CI job. The iOS store tests inject the native
Keychain boundary to exercise failed updates/additions/deletions while checking preserved metadata
and secrets. These tests do not prove device Keychain or Android Keystore behavior; real-device
security-storage validation remains necessary.
`ServerIntegrationTest` runs against a real server only when configured; otherwise JUnit reports it as skipped:

```sh
OTUS_JMAP_TEST_URL=http://127.0.0.1:8080/.well-known/jmap OTUS_JMAP_TEST_USER=admin \
OTUS_JMAP_TEST_PASSWORD=… OTUS_JMAP_TEST_INSECURE=1 ./gradlew :jmap:testAndroidHostTest --rerun-tasks
```

It passed against Stalwart 0.16.24 in bootstrap mode (session discovery, `Mailbox/get`, wrong
password). A third test creates and deletes a mailbox and uploads a blob; it has not been run
against a server yet. Bootstrap mode cannot query mail, so Mail's inbox was checked on a device with a real
account. CI does not run it yet. Complete iOS builds and runtime checks, including the Keychain,
require macOS/Xcode.

Contact-photo lookup now uses RFC 9610 and the blob-download API. See [Mail's avatar guide](https://github.com/reazndev/otus-mail/blob/main/docs/avatars.md) for
capability discovery, exact email matching, public-image options, and generated fallbacks.
The full RFC 9610 client is the `jmap-contacts` module; see [Contacts](contacts.md).

## Client lifetime

The owner of a `JmapClient` calls `close()` when it no longer needs the client. Closing is
idempotent and stops new requests. Existing requests finish before Ktor releases their resources.
The default platform engine belongs to the client. An injected engine belongs to its caller,
which must close it after all clients using it have closed. Short-lived callers can use
`JmapClient().use { client -> ... }`.
