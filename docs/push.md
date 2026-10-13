# Push subscriptions

`com.otus:push` keeps one JMAP push subscription (RFC 8620, section 7.2) per login, pointed at
the endpoint the app's push service gave that login. It has no UI and no platform push code.
Each app connects its own push service, such as a UnifiedPush receiver on Android. See
[decision 0015](decisions/0015-push-notifications.md).

## API

- `PushSubscriptions(connections, logins, store, deviceClientId, types)`: create one per process,
  so its lock covers every caller.
  - `setEndpoint(loginId, PushEndpoint(url, keys))` and `clearEndpoint(loginId)` save what the
    push service reported.
  - `receive(loginId, verification)` keeps the code of a `PushVerification` push for the
    current subscription.
  - `sync()` makes each server match. It creates or replaces subscriptions, sends verification
    codes, renews subscriptions that expire within 3 days for another 7, and deletes them when
    the endpoint is cleared.
  - `status()` reports `Off`, `Waiting`, `Active`, or `Refused` per login.
- `PushStateStore` keeps each login's record as text in app-private storage. Records hold no
  credentials.

## Behavior

- A new subscription asks for `types`. Mail asks for `EmailDelivery`, which changes only when
  mail is delivered (RFC 8621, section 1.5).
- Creating a subscription also deletes this device's earlier ones, found by `deviceClientId`, so
  a reinstall does not leave the server pushing twice.
- A server that answers `unknownMethod` or `forbidden`, or does not create the subscription, is
  marked `Refused`. The next `sync` tries again.
- A server that cannot be reached keeps the login's record until the next `sync`.
- A record of a removed login is dropped without contacting the server, because its credentials
  are gone. The server deletes the subscription when it expires.

## Checks

`PushSubscriptionsTest` covers creation, verification, replacement of an earlier endpoint and
this device's old subscriptions, renewal, refusal, deletion, and an unreachable server.
