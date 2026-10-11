# Avatars

The `avatar` module (`com.otus.avatar`, artifact `com.otus:avatar`) resolves sender and attendee
avatars: JMAP contact photos, optional website icons, and generated initials. It has no UI. The
design system's `Avatar` element draws the result, and `rememberAvatarImage` decodes image bytes.
It moved here from otus-mail in version 0.2.0.

## Implemented API

`AvatarService` (`com.otus.avatar`) is common Kotlin, independent of mail, calendar, Compose, and platform
image types. It resolves an `AvatarRequest(accountKey, email, name)` in this order:

1. A contact photo or logo through the injected `ContactPhotoSource`.
2. A website-domain icon through `AvatarImageLoader`, when `AvatarOptions.domainIcons` is enabled.
   An injected `DomainIconSource` can supply a cached favicon when `domainIconProxy` is also enabled.
3. Generated initials, with an email-derived index for the app's named theme colors.

Every result includes the generated fallback. Image decoding failures also display this fallback.
Email matching trims whitespace and ignores case in the whole address, because real mail servers
treat `John@` and `john@` as one mailbox. Dots and plus tags are preserved. Names do not determine contact identity. Changing a display name
changes initials without changing the address's color.

`AvatarOptions.externalContactImages`, `domainIcons`, and `domainIconProxy` default to false. Embedded contact
images and authenticated JMAP blobs still work. Public image requests use a separate HTTP client
that has no account credentials. Mail enables all three options for inbox senders, so contacts with
external photos and senders without a contact photo can display images automatically. Callers of
the reusable service can keep remote requests disabled. No appearance-setting switch exists yet.

```kotlin
val images = HttpAvatarImageLoader()
val service = AvatarService(contacts = contactPhotos, images = images)
val result = service.resolve(
    AvatarRequest(loginId, "ada@company.example", "Ada Lovelace"),
    AvatarOptions(domainIcons = true),
)
```

Use an app-private, stable login ID for `accountKey`, never the display name or a password. Call
`service.removeAccount(loginId)` after logout or reconnect, and `service.clear()` for an explicit contact refresh. Ordinary inbox reloads retain the cache. Create one service per app process;
close an explicitly owned `HttpAvatarImageLoader` when its owning scope ends.

## Caching and domain icons

The service keeps up to 64 recently used contact/domain records in memory. Contact keys include the
login, normalized email and external-image policy. Website icons use the exact hostname and proxy
policy, shared across senders and accounts. Two addresses at `proton.me` fetch the website icon once;
their individual contact photos still take priority and remain separate. Display names never enter
cache keys, so initials always follow the current name.

App entry points supply `FileAvatarStore` in the app's private cache directory. It survives
process restarts and uses hashed filenames, atomic record replacement, a 32 MiB disk limit and at most
256 records. Every image is capped at 512 KiB. Successful images are reused for 30 days; missing
images are retried after five minutes. Existing images remain visible during an expired/offline
lookup. The OS may reclaim cache files. Removing or reconnecting an account deletes its private
contact records while retaining public website icons. Callers must not mutate image bytes.

Concurrent lookups share a pending result for each contact or domain key. At most eight fetches run
at once. Cancellation of the owner lets waiting callers retry. Cache invalidation also covers
lookups waiting for a fetch permit and prevents old results from repopulating memory or disk.

`HttpWebsiteIconSource` first reads a bounded portion of the website's HTML and tries advertised raster icon links,
prioritizing larger declared sizes and Apple touch icons. Relative links use the final website URL
after redirects. This finds Proton's 180 px touch icon rather than enlarging the 32 px proxy favicon.
If discovery fails, it tries HTTPS `apple-touch-icon.png`, `favicon.png`, and `favicon.ico` on the
sender's exact hostname, then on its registrable website domain from the bundled Public Suffix List.
All direct website attempts together have an 8-second budget. For example, `email.openai.com` falls
back to `openai.com`. An exact root hostname is fetched only once. Multi-label suffixes such as `co.uk`, private hosting suffixes
such as `github.io`, wildcards, and exceptions are respected. Bare public suffixes, IP literals,
internal hosts, and malformed hostnames retain initials. Shared mail providers such as `proton.me`
are eligible for a website logo; this identifies the provider, not the individual sender.

When direct website images fail, Mail's injected fallback requests
`https://icons.duckduckgo.com/ip3/{domain}.ico`. This public favicon cache receives only the website
domain, never a complete email address, saved login ID, password, or account authorization header.
The service has no baked-in cache provider: callers inject `DomainIconSource`, and must enable
`domainIconProxy` as well as `domainIcons`. Contacts always take priority. No message-body images or
email-hash avatar services are used. This fallback also covers websites that block direct requests.

`PublicSuffixDomainResolver` accepts an injected suspend loader for the rules. Mail wiring reads
`files/public_suffix_list.dat` from packaged Compose resources. The parser is common Kotlin,
loads once off the UI thread, and never downloads rules at runtime. The service defaults to using
an exact hostname unless a resolver is injected. Calendar includes the same parser and resource
in its own older copy until it adopts this module. IDNs require an ASCII hostname supplied by the caller.

The list snapshot is `2026-10-01_23-02-52_UTC`, upstream commit
`6cd82aff889e3d64e5e03bc5c1f43da1934a960a`, from
[publicsuffix.org](https://publicsuffix.org/list/public_suffix_list.dat). Source SHA256 is
`e0fe072d26b0536525badea237953ff451c9f8e64c9d02c6daa81a4491d2fc66`.
Rules are lowercased and converted label by label to ASCII IDNA while retaining wildcard and
exception markers. Original comments and the MPL 2.0 notice remain; the license ships as
`public_suffix_list_MPL-2.0.txt`. Each app bundles the list and supplies it to the resolver; refresh both apps' copies together.
ICO display depends on the platform decoder; Android tests cover PNG and bitmap-encoded entries.

The public loader follows at most three redirects, checking each target for HTTPS, a public
hostname, and absence of URL credentials. Relative paths and public CDN redirects are supported.
Image loads refuse credential-bearing URLs, SVG/HTML responses, oversized bodies, and non-HTTPS
URLs. Website discovery reads at most 64 KiB of HTML; it never executes scripts or displays HTML.
The entire load, including redirects, times out after five seconds. Domain icons represent a website or organization;
they are not proof that a sender is authentic. The image cache supports offline display; it does not
synchronize contacts offline.

## Use in the apps

Each app creates one `AvatarService` per process and passes its own login IDs as `accountKey`.
The app chooses which remote requests to allow through `AvatarOptions`, supplies the Public Suffix
List bytes, and injects any `DomainIconSource`. `JmapContactPhotoSource` uses the app's
`JmapConnections` and `LoginStore`, so contact lookups never cross apps. Calendar still carries an
older copy of this code until it adopts the module.

Contact queries filter to readable, subscribed address books and fetch up to 32 candidates per
page. Each candidate must contain the exact normalized email address; server searches may also
match email labels. Lookup continues across pages until it finds a usable photo or reaches an empty
page, including when the server returns fewer candidates than requested. It retains one page at a
time and stops if the query state changes, the server ignores the requested position, or the
10-second search deadline expires. Cancellation from the caller propagates. These bounds can
still leave a photo unavailable during slow or changing searches; normal avatar fallback applies.

## Protocol references and validation

- [RFC 9610, contacts capability and ContactCards](https://www.rfc-editor.org/rfc/rfc9610.html).
- [RFC 9553, contact media](https://www.rfc-editor.org/rfc/rfc9553.html#section-2.6.4).
- [RFC 8620, blob downloads](https://www.rfc-editor.org/rfc/rfc8620.html#section-6.2).

Common tests cover resolution priority, account/options isolation, missing capabilities, exact email
matching, address-book permissions, blobs/data URIs/external URLs, website-domain resolution,
provider icons, icon-cache options and domain-only requests, cache expiry/eviction/invalidation,
cancellation, credential isolation, redirects, and response-size limits. Persistent-cache tests cover
process recreation, bounded disk usage, corrupt records and account removal.
Contact search tests also cover later-page exact matches, server-clamped page sizes, exhaustion,
changing queries, stalled pagination, search deadlines, and caller cancellation.
Mail's inbox tests cover restoring avatars before network work; see
[Mail's avatar guide](https://github.com/reazndev/otus-mail/blob/main/docs/avatars.md).
`RememberAvatarImageTest` and `DecodedAvatarCacheTest` in the design system cover decoding.
iOS runtime validation requires macOS/Xcode.
