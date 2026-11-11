# JMAP

The `jmap` module (`com.otusfoss.core.jmap`, artifact `com.otusfoss.core:jmap`) is a provider-independent client for
RFC 8620 (core) and login storage. It has no UI and no app
code, so Otus Mail and Otus Calendar can both use it. Mail-specific types live in the Mail app.
Developed and checked against Stalwart. It moved here from otus-mail at version 0.1.0.

## Module rules

- Kotlin explicit API mode: every public declaration is deliberate. Depends only on Ktor client
  core, kotlinx.serialization, coroutines, and `json-types`. Apps supply the HTTP engine (OkHttp on Android, Darwin
  on iOS).
- The architecture check enforces: package root `com.otusfoss.core.jmap`; no Compose or lifecycle; no
  references to `com.otusfoss.mail` or `com.otusfoss.calendar`; no platform APIs in common code. File length,
  suppression, wildcard-import, detekt, and ktlint rules apply as everywhere else.
- Apps use it as `api(libs.otus.jmap)` from their `shared` module; see
  [Using the libraries from an app](consuming.md).

## API

- `JmapClient(engine, policy)`: one per process. `session(url, auth, trustedOrigins)` discovers the
  session; `request { … }`, `send { … }`, and calls on a session run method calls. A sign-in passes
  `requireTrustedBlobOrigins = true` so that blob hosts on another origin also need approval.
- `JmapConnection`: one signed-in server. Caches the session, refetches it when a response reports
  a different `sessionState` (RFC 8620, section 3.4), and drops it after a transport failure.
  Upload and download failures also invalidate the session for the next attempt, except
  `BlobNotFound` and `RedirectedElsewhere`, which a new session would not change. Failed requests
  are never automatically replayed, and a late failure cannot discard a newer cached session.
  `call(method)` for one call, `request { …; call(x) }` returning the last call's typed result, and
  `send { … }` for several results.
- `JmapConnections(client, logins)`: one connection per saved login; `forget(id)` after it changes.
  Credential loading and cache insertion share the invalidation lock, including concurrent first access.
  `account(login, capability)` returns a `JmapLoginAccount` (login, connection, session, and the
  account id for that capability) and throws `Unsupported` when no account offers it;
  `account(loginId, capability)` does the same for a saved login, or returns null once it was
  removed. `savedLogins()` reads the logins off the main thread.
- `JmapResponse[call]` decodes the explicit response and validates its method name.
  `responses(call)` exposes every response in wire order, including implicit calls and errors;
  an implicit failure does not replace the explicit method's result.
- Typed methods for any data type (RFC 8620, section 5): `GetMethod`, `QueryMethod`, `ChangesMethod`,
  `QueryChangesMethod`, and `SetMethod`, with `JmapType(name, capability, serializer)`.
  Result references (section 3.7) chain calls in one request:
  `JmapIds.Result(query.resultOf("/ids"))`. Subclass `JmapMethod` for anything else.
- `QueryChangesMethod(type, accountId, sinceQueryState, filter, sort, maxChanges, extra)` updates a
  cached `/query` result (section 5.6). Pass the filter and sort of the query that returned the
  state. `QueryChangesResult` holds the `removed` ids and the `added` `AddedItem`s in index order.
  Splice the removed ids out, then the added ids in, lowest index first. `extra` carries `upToId`
  and `calculateTotal`. `cannotCalculateChanges` and `tooManyChanges` arrive as `MethodError`. The
  RFC's own example answers `"added": null`, which reads as an empty list.
- `JmapFilter` is the `filter` of a `/query` or `/queryChanges` (section 5.5): a type-specific
  `JmapFilter.Condition`, or a `JmapFilter.Operator` that combines filters with `AND`, `OR`, or
  `NOT`. `filterCondition(name, value)` builds a condition. `allOf`, `anyOf`, and `noneOf` take
  filters or a list. `allOf` and `anyOf` of a single filter return it unchanged, which means the same.
  `QueryMethod` and `QueryChangesMethod` take a `JmapFilter`. A condition with an `operator`
  property throws.
- `jmapPatch { set(value, "keywords", "chopin"); remove("name") }` builds a `/set` PatchObject
  (section 5.3). Each path segment escapes `~` and `/` as JSON Pointer does, and `patchPath` from
  `json-types` writes one such key. Two paths where one is a prefix of the other throw
  (`overlappingPatchPath`), because the server would answer `invalidPatch`. `jscontact` reads and
  checks its patches with the same `json-types` rules. `include` adds the entries of another patch.
- `JmapSort` takes a `collation`, an RFC 4790 algorithm from the core capability's
  `collationAlgorithms`. It is sent only when set.
- `QueryResult.canCalculateChanges` says whether `/queryChanges` can follow the query.
  `QueryResult.limit` holds the limit the server applied when it set or changed one, and callers
  page by that size. Paging arguments such as `position`, `anchor`, and `calculateTotal` go in
  `extra`, as [decision 0016](decisions/0016-contacts-modules.md) explains.
- `JmapSession.maxObjectsInGet`, `maxObjectsInSet`, and `maxCallsInRequest` read the core limits
  (section 2). Each is null when the server does not say or sends a malformed value.
  `getBatch`, `setBatch`, and `callBatch` are the sizes to send: the limit, at most 256 objects or
  16 calls, which is also the default without one.
- `UtcDateSerializer` (in `json-types`) reads and writes a UTCDate (section 1.4) as a
  `kotlin.time.Instant`. It writes the `Z` form with a fraction of a second only when it is not
  zero, without trailing zeros, such as `2014-10-30T06:12:00.5Z`. It reads any RFC 3339 offset.
  `Instant` holds neither a leap second nor more than nine fraction digits, so `23:59:60` reads as
  `23:59:59.999999999`, and reading drops the digits after the ninth. A seconds field of three
  digits, such as `:600`, is malformed. A year outside 0000 to 9999 fails in both directions,
  because RFC 3339 cannot write it. `jscontact` and `ContactCardFilter` use the same serializer.
- Failures: `JmapException.category` reads any failure as one `JmapErrorCategory` (`Auth`,
  `Permission`, `Credentials`, `Offline`, `Unsupported`, or `Server`), so apps map it once.
  `MethodError.isTemporary` says whether `serverUnavailable`, `serverFail`, or `serverPartialFail`
  may pass on a later try. `jmapCallOr(fallback) { … }` returns the fallback when the server fails
  or the saved logins cannot be read, for optional work such as a count.
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
  percent-encoded, including slashes. A 404 throws `BlobNotFound`, because the server does not
  have that blob; other failure statuses map as for API requests.
- `downloadStream(accountId, blobId, mediaType, name, receive)` reads a blob the same way without a
  size limit, handing each piece of up to 64 KiB to `receive` as it arrives, so an app can write a
  large attachment to disk without holding it. The caller bounds what it accepts, such as by the
  part's size; an exception from `receive` or a cancel ends the transfer.
- `upload(accountId, bytes, mediaType)` stores a blob through the session's `uploadUrl` (section 6.1)
  with the same https and trusted-origin checks as downloads. Redirects are not followed. A blob over
  the advertised `maxSizeUpload` throws `UploadTooLarge` before anything is sent.
- `JmapPolicy` time limits: `connectTimeoutMillis` (15 s) for each connection, `requestTimeoutMillis`
  (30 s) for a whole session or API request, and `socketTimeoutMillis` (30 s) for any request that
  sends or receives nothing. Uploads and downloads take longer the larger the blob and the slower the
  link, so only the connection and socket limits apply to them.
- `RequestFailed` keeps the problem `type` and, for `urn:ietf:params:jmap:error:limit`, the `limit`
  the server names, such as `maxSizeRequest` (section 3.6.1).
- `JmapSession.capabilityObjects` and `JmapAccount.capabilityObjects` keep each capability's object,
  such as the core limits (`maxObjectsInSet`, `maxSizeUpload`), the mail limits, or the supported Sieve
  extensions. Apps decode the parts they need; malformed values read as unknown.
- Before sending, the client checks every capability against the session and each account a call
  touches. It throws `Unsupported` instead of sending a request the server cannot serve.
- Logins (`com.otusfoss.core.jmap.login`): `JmapLogin` identifies a server and user, not a JMAP account.
  Each app resolves its own account per capability with `JmapConnections.account`, so one login can
  serve mail and calendars. `LoginStore` keeps secrets apart from the list. `LoginLifecycle` is what
  follows a login's life: `connected`, `removing`, `removeFailed`, and `removed`; see
  [shared features](shared-features.md#accounts).

## Security model

| Threat | Mitigation |
| --- | --- |
| Credentials sent in clear | Every URL must be https: the session URL, each redirect, `apiUrl`, and each expanded blob URL. `JmapPolicy.allowInsecureHttp` exists only for local test servers. |
| Credentials leaked by a redirect | Authorization headers go only to the origin the user entered and origins the user approved. Discovery can probe another origin without authorization, then raises `RedirectedElsewhere`; Mail asks the user and stores the approval. RFC 3986 resolution prevents the old query and URL credentials from being copied to the new host. RFC 8620 allows `downloadUrl` and `uploadUrl` on another host. With `requireTrustedBlobOrigins`, which the `accounts` sign-in passes, discovery checks their origins, with every template variable filled by a placeholder, and raises `RedirectedElsewhere` for one the user has not approved, so the user approves blob hosts before the login is saved. Without it, as when a saved connection rediscovers its session, an unapproved blob host fails only blob calls. The API and blob endpoints require a trusted origin before each request, checked on the expanded URL. |
| Hostile or broken responses | Responses are capped at 16 MiB, except a streamed blob, which the caller bounds as it receives it. Envelope invocations are checked, and decoding failures become `NotJmap`. Custom decoders must use serializers, checked JSON accessors, or `require`; serialization, argument, cast, and missing-key errors are normalized. Cancellation and other programming errors propagate. |
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
| 2 | `isJmapServer` probes a session URL without credentials, following the same redirects: a 401 or a session with the core capability counts as JMAP | `JmapServerProbeTest` |
| 3.2–3.4 | Request envelope with `using`, invocations, `sessionState` | `JmapRequestTest`, `JmapConnectionTest` |
| 3.6.1 | Request-level problem details become `RequestFailed(type, limit)` | `JmapDiscoveryTest` |
| 3.6.2 | Method-level errors become `MethodError(type)` for that call only | `JmapRequestTest` |
| 3.7 | Result references | `JmapRequestTest` |
| 5.1, 5.2, 5.5 | `/get`, `/changes`, `/query` | `JmapRequestTest`, Mail tests |
| 5.3 | `/set` creations, patches, destructions, `ifInState`, creation references, per-object errors and outcome validation | `JmapSetTest`, `JmapSetValidationTest` |
| 5.3 | PatchObjects with escaped JSON Pointer keys; overlapping paths rejected before sending | `JmapPatchTest` |
| 5.5 | FilterOperator trees, Comparator `collation`, `canCalculateChanges`, and the server's `limit` | `JmapFilterTest`, `JmapQueryTest` |
| 5.6 | `/queryChanges` with the cached filter and sort, `maxChanges`, and `extra`; `added` in index order | `JmapQueryChangesTest` |
| 2 | Blob endpoints on another origin are approved at sign-in and fail only blob calls until then | `JmapDiscoveryTest`, `JmapBlobRecoveryTest`, `JmapAccountRepositoryTest` in `accounts` |
| 6.1 | Authenticated uploads through the advertised URI template, within `maxSizeUpload`, without a whole-call time limit | `JmapUploadTest`, `JmapTimeoutTest` |
| 6.2 | Bounded authenticated blob downloads through the advertised URI template, with trusted-origin checks, `BlobNotFound` for a 404, and no whole-call time limit; streamed downloads with the same checks | `JmapDownloadTest`, `JmapDownloadStreamTest`, `JmapBlobRecoveryTest`, `JmapTimeoutTest` |
| RFC 7617 | Basic credentials; a colon in the username is rejected | `JmapLoginTest` |
| 1.3, 5.5 | Query counters support the full unsigned 53-bit range | `JmapDecodingTest` |
| 1.4 | UTCDate written in its `Z` form without trailing fraction zeros; any RFC 3339 offset, a leap second, and a fraction longer than nine digits read; a three-digit seconds field and years outside 0000 to 9999 refused | `UtcDateSerializerTest` in `json-types` |
| 2 | `maxObjectsInGet` from the core capability | `JmapSessionLimitsTest` |
| 7.1, 7.2 | `StateChange` and `PushVerification` messages; `PushSubscription/get` and `/set` without an account or state; https push URLs | `JmapPushTest` |
| RFC 9749 | The VAPID public key from the session | `JmapPushTest` |
Not implemented: `/copy` and the RFC 9670 sharing types, removed on 2026-11-06 for having no
caller (see [removed APIs](removed-apis.md)), `Blob/copy` (section 6.3), Blob/upload (RFC 9404),
and the EventSource stream (section 7.3). The client does not enforce the core limits; callers
size their calls with `getBatch`, `setBatch`, and `callBatch`.

## Use in the apps

Apps depend on `com.otusfoss.core:jmap` and define their own data types with `JmapType` and the matching
capability, such as `MailboxType` in Mail or a `CalendarEvent` type in Calendar, and resolve the
account with `JmapConnections.account`. Each installed app passes its own storage names to `AndroidLoginStore`
and `IosLoginStore`, so apps never share logins. Mail keeps the names of its first release
(`otus_accounts` and `otus.account.credentials` on Android, `otus.accounts` and
`otus.mail.account` on iOS) so earlier accounts keep working.

## Checks

`./gradlew :jmap:testAndroidHostTest` runs protocol tests with Ktor's mock engine, concurrent
connection-cache tests, and Android preference-store tests with an injected cipher. It is required
by `checkAll` and the Android CI job. Tests cover explicit and implicit responses, method-name mismatches,
network failures and cancellation, capability checks, changes, account replacement, storage failures,
and corrupt-list preservation. Mail's app tests also cover storage-error recovery and same-named accounts.
`JmapBlobRecoveryTest` covers endpoint rediscovery, cancellation, late failures after session replacement,
and the failures that keep the session. `JmapTimeoutTest` runs slow servers in virtual time.

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
