# Avatars

The `avatar` module (`com.otus.avatar`, artifact `com.otus:avatar`) resolves sender and attendee
avatars: JMAP contact photos, optional website icons, and generated initials. It has no UI. The
design system's `Avatar` element draws the result, and `rememberAvatarImage` decodes image bytes.
It moved here from otus-mail in version 0.2.0.

## Implemented API

`AvatarService` (`com.otus.avatar`) is common Kotlin, independent of mail, calendar, Compose, and platform
image types. It resolves an `AvatarRequest(accountKey, email, name, brandDomain)` in this order:

1. A contact card's avatar through the injected `ContactAvatarSource`: an `AvatarStyle` the user
   built in Otus, or else a photo or logo. A style comes back in `ResolvedAvatar.style`.
2. A brand logo from `AvatarOptions.iconProxy.brandLogoUrl`, when the request's `brandDomain` is
   on the sender's website domain. See [brand logos](#brand-logos).
3. A website-domain icon through `AvatarImageLoader`, when `AvatarOptions.domainIcons` is enabled:
   from `iconProxy.iconUrl` when the options name an icon proxy, else from the website itself.
4. Generated initials, with an email-derived index for the app's named theme colors.

Every result includes the generated fallback. Image decoding failures also display this fallback.
Email matching trims whitespace and ignores case in the whole address, because real mail servers
treat `John@` and `john@` as one mailbox. Dots and plus tags are preserved. Names do not determine contact identity. Changing a display name
changes initials without changing the address's color.

`AvatarOptions.externalContactImages` and `domainIcons` default to false, and `iconProxy` to null. Embedded contact
images and authenticated JMAP blobs still work. Public image requests use a separate HTTP client
that has no account credentials. Mail enables all three options for inbox senders, so contacts with
external photos and senders without a contact photo can display images automatically. Callers of
the reusable service can keep remote requests disabled.

```kotlin
val images = HttpAvatarImageLoader()
val service = AvatarService(contacts = contactPhotos, images = images)
val result = service.resolve(
    AvatarRequest(loginId, "ada@example.com", "Ada Lovelace"),
    AvatarOptions(domainIcons = true),
)
```

Use an app-private, stable login ID for `accountKey`, never the display name or a password. Call
`service.removeAccount(loginId)` after logout or reconnect, and `service.clear()` for an explicit contact refresh. Ordinary inbox reloads retain the cache. Create one service per app process;
close an explicitly owned `HttpAvatarImageLoader` when its owning scope ends.

## Caching and domain icons

The service keeps up to 64 recently used contact/domain records in memory. Contact keys include the
login, normalized email and external-image policy. Website icons use the registrable website domain
and proxy policy, shared across senders and accounts. Two addresses at `proton.me` fetch the website
icon once, and so do `a@mail.example.com` and `b@news.example.com`;
their individual contact photos still take priority and remain separate. Display names never enter
cache keys, so initials always follow the current name.

App entry points supply `FileAvatarStore` in the app's private cache directory. It survives
process restarts and uses hashed filenames, atomic record replacement, a 32 MiB disk limit and at most
256 records. Every image is capped at 512 KiB. Successful images are reused for 30 days. A contact
or icon-proxy lookup that found nothing is retried after five minutes, and a website that was asked
directly is asked again after a day, as the [brand logos](#brand-logos) section explains. Existing
images remain visible during an expired/offline lookup. The OS may reclaim cache files. Removing or reconnecting an account deletes its private
contact records while retaining public website icons. Callers must not mutate image bytes.
The store reads the size and age of its records once, on its first write, and keeps them in
memory, so a write deletes the oldest records without opening the others. Use one store per
directory: records another store writes there are not counted until the next process.

Concurrent lookups share a pending result for each contact or domain key. At most eight fetches run
at once. Cancellation of the owner lets waiting callers retry. Cache invalidation also covers
lookups waiting for a fetch permit and prevents old results from repopulating memory or disk.

`HttpWebsiteIconSource` first reads a bounded portion of the website's HTML and tries advertised raster icon links,
prioritizing larger declared sizes and Apple touch icons. Relative links use the final website URL
after redirects. This finds Proton's 180 px touch icon rather than enlarging the 32 px proxy favicon.
It reads the `<link>` tags before `</head>` in one pass that skips comments and tags longer than
2 KiB, so its time grows linearly with the page, and it parses on `Dispatchers.Default`, never on
the caller's thread. If discovery fails, it tries HTTPS `apple-touch-icon.png`, `favicon.png`, and `favicon.ico`.
The service sends all of these requests to the sender's registrable website domain from the bundled
Public Suffix List, never to the exact hostname. All direct website attempts together have an
8-second budget. For example, `noreply@email.openai.com` fetches only from `openai.com`. Multi-label suffixes such as `co.uk`, private hosting suffixes
such as `github.io`, wildcards, and exceptions are respected. Bare public suffixes, IP literals,
internal hosts, and malformed hostnames retain initials. Shared mail providers such as `proton.me`
are eligible for a website logo; this identifies the provider, not the individual sender.

## Icon proxies

`AvatarOptions.iconProxy` names an `IconProxy`: a service that fetches icons for the device, so the
sender's website never sees it. Its `iconUrl` and optional `brandLogoUrl` are HTTPS URL templates
with `{domain}` in place of the registrable website domain, such as
`https://icons.duckduckgo.com/ip3/{domain}.ico`. While a proxy is set, the service asks only the
proxy. A miss leaves initials and never falls back to the website. The proxy receives the website
domain, never a complete email address, saved login ID, password, or account authorization header.
Its requests go through the same public loader as website icons; `isPublicAvatarUrl` lets an app
check an address against the loader's rules before saving it. An `IconProxy` with a null `iconUrl`
stands for a service the user chose but cannot reach: it loads no icons, and the website is still
never asked, even for a contact styled with `AvatarMark.Website`. Icon cache keys include the proxy
URL, so changing proxies looks again.

Icon services such as Bulwark answer a 1×1 PNG for a domain without an icon. The service treats any
PNG one pixel wide or tall as no image. Contacts always take priority. No message-body images or
email-hash avatar services are used.

### Brand logos

A brand logo (BIMI) says who owns a domain, not that a given message came from it. The caller
therefore sets `AvatarRequest.brandDomain` only for a message that passed DMARC for its From domain,
as the user's server recorded it. The service asks `brandLogoUrl` only when `brandDomain` resolves to
the same registrable domain as the sender's address, and asks for that registrable domain, never the
exact host. Logos are cached apart from icons per domain and proxy, and a request without a
`brandDomain` never reads them, so a spoofed message from the same address shows the icon. A missing
logo falls back to the icon.

Fetching a website icon tells that website the device's IP address and when the inbox showed the
sender. A sender who gave each recipient a unique subdomain, such as `u-12345.example.com`, could
otherwise use the icon request as a read receipt. The service therefore fetches and caches icons
only per registrable domain, so every subdomain shares one request to `example.com`. The registrable
domain still learns the device's IP address once per cache period when it has an icon, and once a
day when it has none or does not answer, which is why `domainIcons` is off by default and callers
may keep it off. An icon proxy's misses are retried after five minutes, because the proxy, not the
sender's website, sees those requests.

`PublicSuffixDomainResolver` accepts an injected suspend loader for the rules. Mail wiring reads
`files/public_suffix_list.dat` from packaged Compose resources. The parser is common Kotlin,
loads once off the UI thread, and never downloads rules at runtime. Without an injected resolver,
the service treats the whole hostname as the website domain. Calendar includes the same parser and resource
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

A public hostname is an ASCII DNS name of at least two labels whose last label is alphabetic or an
`xn--` IDN label. The same check covers the sender's domain and every request of the public
loader: the first URL, each redirect, advertised icon links, and external contact photos. The loader
also requires HTTPS on the default port 443. A website therefore cannot send the device to a private
or special-use name on the user's network, but see the DNS limit below. The check refuses:

- single-label names, such as `printer`;
- IP literals in every form the URL parser accepts, such as `192.168.1.1`, `127.1`, `0x7f.0x1`, and
  `[::1]`; resolvers read a numeric last label as an IPv4 address, and no real top-level domain has
  one;
- special-use and reserved names: `localhost`, `test`, `invalid`, and `example` (RFC 6761), `local`
  (RFC 6762), `onion` (RFC 7686), `alt` (RFC 9476), `internal` (reserved by ICANN for private use),
  and all of `arpa`, which includes `home.arpa` (RFC 8375);
- the widespread private top-level names `lan`, `home`, `corp`, `intranet`, `private`, and
  `localdomain`.

Suffixes match whole labels and ignore case, so `lan` and `printer.lan` are refused while `plan.com`
is allowed. Names with a trailing dot, percent-encoding, or non-ASCII characters are refused rather
than normalized, so they cannot bypass the list.

The check sees only the name, never the address it resolves to. A public name whose DNS answer is a
private, loopback, or link-local address, such as a wildcard DNS service or a record the sender
controls, still receives the request, so a website can direct icon requests into the user's network
that way. Common code has no resolver to check the address; refusing such answers needs a DNS check
in each platform's HTTP engine, which the module does not have yet.

## Use in the apps

Each app creates one `AvatarService` per process and passes its own login IDs as `accountKey`.
The app chooses which remote requests to allow through `AvatarOptions`, supplies the Public Suffix
List bytes, and names any `IconProxy`. `JmapContactAvatarSource` uses the app's
`JmapConnections` and `LoginStore`, so contact lookups never cross apps. Calendar still carries an
older copy of this code until it adopts the module.

Contact queries ask for `id`, `addressBookIds`, `emails`, `media`, and `vCard`. Stalwart returns a
card's extra vCard properties, where the style lives, only when `vCard` is asked for by name; a
server that rejects `vCard` with `invalidArguments` gets the query again without it. They search
readable address books that are subscribed or writable, because Stalwart leaves the user's own
books unsubscribed, and fetch up to 32 candidates per
page. Each candidate must contain the exact normalized email address; server searches may also
match email labels. On each page, cards in the user's writable books come first, then cards whose
avatar Otus wrote, so a shared card never hides the user's own. Lookup continues across pages until it finds a usable photo or reaches an empty
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
cancellation, credential isolation, redirects, refused private and special-use hosts,
response-size limits, linear-time parsing of hostile HTML, and the miss lifetimes of websites and
proxies. Persistent-cache tests cover process recreation, bounded disk usage, pruning by age after a
restart, corrupt records and account removal.
Contact search tests also cover later-page exact matches, server-clamped page sizes, exhaustion,
changing queries, stalled pagination, search deadlines, and caller cancellation.
Mail's inbox tests cover restoring avatars before network work; see
[Mail's avatar guide](https://github.com/reazndev/otus-mail/blob/main/docs/avatars.md).
`RememberAvatarImageTest` and `DecodedAvatarCacheTest` in the design system cover decoding.
iOS runtime validation requires macOS/Xcode.

## Avatars of the user's own addresses

`OwnAvatars` reads and saves the avatar of one of the user's own addresses, and `JmapOwnAvatars`
keeps it on a self card: a contact card in the user's own address book with that address. Saving
finds a card with the address in a writable book, preferring one whose avatar Otus wrote, or
creates one in the default book with the address and the account name. A patch path needs its
parent on the server (RFC 8620, section 5.3), so a card without `media` or `vCard` gets the whole
map or object instead.

- A photo is a JSContact `media` entry of kind `photo` with a `data:` URI, so other contact apps
  show it too. Stalwart refuses a `blobId` in `media` ("blobIds in media is not supported"). Saving a
  photo replaces the card's other photos and drops the style.
- An `AvatarStyle` is a color and an `AvatarMark`. The color is a folder color name or a custom
  `#RRGGBB`. The mark is up to `MAX_AVATAR_LETTERS` letters, an icon name from `AvatarSymbols`, or
  `Website`. `Website` shows the website icon of the address's domain even when
  `AvatarOptions.domainIcons` is off, because the user picked it for their own address;
  `ResolvedAvatar` then has both the icon and the style. Lookups honor `Website` only on cards in
  books the user may write, so a shared card cannot turn website icons on. A style is stored as the vCard extension
  property `AVATAR_STYLE_PROPERTY` (`x-otus-avatar`) in the card's `vCard.properties` (RFC 9555),
  with a JSON text value such as `{"color": "Blue", "icon": "work"}`. Stalwart refuses unknown
  top-level card properties ("Invalid property."), so a JSContact vendor property does not work there.
  Saving a style removes the card's photos, so every app agrees there is none. Other contact apps
  keep the property but show no avatar.
- Saving null removes the photos and the style but keeps the card.
- `OwnAvatarException` names why a save failed: `NoContacts`, `ReadOnly`, or `Failed`.

A save clears the login's cached avatars, so the next `resolve` reads the new one. The disk cache
stores a style as its JSON. The apps map color names to `FolderColor` and mark names to
`AvatarSymbols`, so the module stays free of UI types. An `AvatarPhotoPicker` from the app supplies
photos, already cropped and small enough to upload.
