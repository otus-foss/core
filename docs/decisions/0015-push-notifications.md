# 0015: Push notifications without an Otus server

## Decision

Mail and Calendar learn about new data straight from the user's JMAP server. Otus runs no push
server or gateway for Android delivery. The Apple implementation on `feat/apple-push` adds an
optional APNs bridge, as [decision 0017](0017-apple-push.md) records. The Android decision follows
[Mail #18](https://github.com/reazndev/otus-mail/issues/18).

The server tells the app that data changed through JMAP push (RFC 8620, section 7). A push holds
only a `StateChange`: account ids, data type names, and new state strings. The app then fetches
the changes itself. With `PushSubscription`, the server POSTs each message to a Web Push URL
(RFC 8030). The app always sends encryption keys (RFC 8291) when it subscribes, so a push service
such as ntfy.sh or Google sees only ciphertext, size, and timing.

The Android delivery modes are listed below. Mail implements background checks, UnifiedPush,
and Google delivery on `feat/google-push`. Calendar adoption and always connected mode remain
planned. The user picks one delivery mode per app.

| Mode | How it works | Needs |
| --- | --- | --- |
| Background check | Android WorkManager checks for changes about every 15 minutes. | Nothing. This is the default. |
| UnifiedPush | The server POSTs to the user's UnifiedPush distributor, such as ntfy, Sunup, or NextPush, which wakes the app. | A distributor app, and a server with `PushSubscription`. |
| Google | UnifiedPush's embedded FCM distributor, built into the Android app, receives the push through Google's FCM. | A server that advertises VAPID (RFC 9749). User-configured gateways are not built yet. |
| Always connected | A foreground service keeps an EventSource stream open. | A server with EventSource. Experimental; see below. |

UnifiedPush is the custom setup. A user who self-hosts runs their own ntfy or Sunup server and
points the distributor at it; Otus only sees the endpoint URL the distributor hands over. Google
needs no setup when the server signs pushes with VAPID. Otherwise FCM rejects the push. A
user-configured gateway remains planned; until then Mail shows that Google delivery is
unsupported and keeps background checks. Otus does not offer a
public gateway.

The Settings page shows the active mode and whether each account's server pushes. Planned
additions: when the last push arrived, and a button that sends a test notification, so a
self-hosted setup can be checked.

## Builds

The original plan split Play and F-Droid builds to exclude Google's proprietary FCM SDK. The
embedded distributor's current release, 3.1.0, contains no proprietary SDK and needs no Firebase
project configuration. Mail's `feat/google-push` includes it in existing Android builds and only
offers Google delivery on devices with Play services. Separate product flavors are unnecessary
for this integration. See the [upstream library](https://unifiedpush.org/kdoc/embedded_fcm_distributor/).

The Apple implementation on `feat/apple-push` adds APNs registration, an embedded notification
extension and a deployable bridge. It is not deployed or device-verified. See
[decision 0017](0017-apple-push.md) for the platform requirements and limits.

## Structure

- `com.otus:jmap` holds the wire types: `PushSubscription` methods, `PushMessage`, and the VAPID
  key from the session. Added in this change.
- `com.otus:push` holds subscription upkeep: create, verify, renew before `expires`, and destroy.
  Each app passes the data types it watches. The UnifiedPush connector library decrypts Web Push
  messages, so Otus has no crypto code. The Android receiver and the background check stay in
  Mail until Calendar needs them.
- Mail owns the embedded FCM dependency and Android registration. Extract shared code when
  Calendar consumes it, as the library architecture rules require.
- `com.otus:settings` will hold the Notifications page.
- Each app owns its notifications: channels, grouping, preview privacy, actions, and what a tap
  opens. Mail and Calendar do not merge notifications, and Calendar reminders stay local.

## Server support to verify

Real servers differ, and the app must read the session before choosing a mode. Known so far:

- Stalwart added VAPID in [0.16.14, released July 20, 2026](https://github.com/stalwartlabs/stalwart/releases/tag/v0.16.14).
  Earlier reports that it lacked VAPID are outdated. Mail still checks the advertised session
  capability; the user's installed version and live Google delivery have not been verified.
- Fastmail, Cyrus, and Apache James are not checked yet. Record their push capabilities here
  before Mail ships a push mode.
- The [JMAP emailpush draft](https://datatracker.ietf.org/doc/draft-ietf-jmap-emailpush/) would let
  a push carry enough of a new email to show a notification without a fetch. Otus does not use it
  while it is a draft.

## Limits

Android may delay background work in Doze and app standby, so the background check is not
real time. Android 15 limits `dataSync` foreground services to 6 hours a day, so the always
connected mode ships only if a device test shows it works under that limit. No mode asks for a
battery exemption by default. A missed push loses no data, because the next check or push fetches
everything since the last saved state.

## Order

1. Wire types in `com.otus:jmap`, and this record. Done.
2. Mail notifications on the background check, with a saved state per account and
   `Email/changes`. Done.
3. UnifiedPush. Built with `com.otus:push`; the device test against a self-hosted ntfy, ntfy.sh,
   and Sunup is still open.
4. Google delivery is implemented on Mail `feat/google-push`, pending merge and a device test.
   It reads each server's VAPID key, replaces registration after key rotation, and shows account
   registration failures. Gateways remain unimplemented.
5. A device test of the always connected mode.
6. Calendar uses `com.otus:push`.
