# 0017: Apple push through an APNs bridge

## Status

Implementation on Mail and Meta `feat/apple-push`, pending review, Apple signing and device
validation. The user requested the Apple implementation after reviewing the bridge requirement.
No service has been deployed and no Apple signing credentials have been provisioned.

## Decision

Native iOS delivery uses APNs. Mail includes an optional bridge in its repository, an iOS
registration adapter and a notification service extension. The build selects an HTTPS bridge
origin; an empty origin leaves Apple push unavailable. Android keeps its existing direct
UnifiedPush and embedded Google delivery paths.

This changes decision 0015's no-server rule for Apple delivery. Hosting remains a deployment
choice. The bridge operator must hold APNs credentials for the developer account that signs the
app. A self-hosted custom build can use its owner's credentials. Do not distribute Otus's Apple
signing key to arbitrary mail servers or embed it in the app.

The JMAP server encrypts each notice for the device. The bridge holds APNs device tokens, random
routes and short-lived ciphertext, not mail passwords or push decryption keys. It converts a Web
Push POST into an APNs alert with an opaque message identifier. The device fetches and decrypts
the notice. The first version shows generic new-mail alerts and opens Mail; message previews and
per-email navigation remain future work.

VAPID is not required for this bridge. Push URLs are private bearer capabilities. The deployment
must provide TLS and ingress rate limits. The bridge also limits route and message storage.

## Ownership

Mail owns the Swift APNs adapter, CryptoKit Web Push decoder, Keychain group, extension and
bridge. Calendar does not consume these yet. Portable JMAP subscription upkeep remains in
`com.otus:jmap-push`; its new `beforeCreate` callback lets a transport buffer verification notices
before every subscription creation, including expired subscriptions replaced during renewal.
Other transports retain their existing behavior by leaving the callback unset.

The bridge sends no alert until the app verifies the subscription. A failed pause prevents the
library from creating a subscription and sending a verification message to an active alert route.
The callback must not call back into `PushSubscriptions`, whose sync lock is held.

## Validation and limits

Mail's [Apple push guide](https://github.com/otus-foss/otus-mail/blob/feat/apple-push/docs/apple-push.md)
records build configuration, bridge deployment, storage retention, lifecycle behavior and tests.
iOS background refresh is best effort. A subscription may expire if the app stays closed and
background refresh does not run. The user must reopen Mail to restore delivery in that case.

A Kotlin iOS compile is not an Xcode app build. Swift interoperability, CryptoKit vectors,
extension embedding, signing and live APNs delivery require validation on a Mac and iPhone
before release. No iOS periodic 15-minute notification check is promised.
