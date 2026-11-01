# Push subscriptions

`com.otus:jmap-push` keeps one JMAP push subscription (RFC 8620, section 7.2) per login, pointed at
the endpoint the app's push service gave that login. It has no UI and no platform push code.
Each app connects its own push service, such as a UnifiedPush receiver on Android. See
[decision 0015](decisions/0015-push-notifications.md).

## Module rename in 0.10.0

The module is now `jmap-push` to identify its JMAP subscription responsibility. Replace the Maven
dependency `com.otus:push` with `com.otus:jmap-push`, and replace `com.otus.push` imports with
`com.otus.jmap.push`. In an Otus app's version catalog, rename `otus-push` to `otus-jmap-push`
and use `libs.otus.jmap.push`. Gradle tasks now use the `:jmap-push:` prefix.

The public type names and behavior are unchanged. Existing stored subscription records, app
storage keys, and platform delivery services keep their formats and identities.

## API

- `PushSubscriptions(connections, logins, store, deviceClientId, types, beforeCreate = { ... })`: create one per process,
  so its lock covers every caller.
  - `setEndpoint(loginId, PushEndpoint(url, keys))` and `clearEndpoint(loginId)` save what the
    push service reported.
  - `receive(loginId, verification)` keeps the code of a `PushVerification` push for the
    current subscription.
  - `beforeCreate(loginId)` runs immediately before creating a subscription that sends a
    verification notice. Apple uses it to pause alerts. Throwing aborts creation; do not call
    subscription methods from the callback, since the sync lock is held.
  - `sync()` makes each server match, asking all servers in parallel. It creates or replaces
    subscriptions, sends verification codes, renews verified subscriptions past half their granted
    lifetime for another 7 days, and deletes them when the endpoint is cleared.
  - `forget(loginId)` deletes a login's subscription and record. Call it before the login's
    credentials are removed; `JmapAccountRepository` takes it as `beforeRemove`.
  - `status()` reports `Off`, `Waiting`, `Active`, or `Refused` per login, without waiting for a
    running `sync`.
- `PushStateStore` keeps each login's record as text in app-private storage. Records hold no
  credentials.

## Behavior

- A new subscription asks for `types`. Mail asks for `EmailDelivery`, which changes only when
  mail is delivered (RFC 8621, section 1.5).
- Creating a subscription also deletes this device's earlier ones, found by `deviceClientId`, so
  a reinstall does not leave the server pushing twice.
- A server that answers `unknownMethod` or `forbidden`, or does not create the subscription, is
  marked `Refused`. The next `sync` tries again. A push URL that is not https is refused without
  asking the server, and the login's earlier subscription is deleted.
- A subscription still unverified an hour after creation is created again, because the server
  pushes nothing until the client returns the code. An unverified subscription is not renewed, so
  this holds even when the server grants less than two hours. An update the server rejects, such
  as a wrong code, also starts a new subscription.
- A server that cannot be reached keeps the login's record until the next `sync`.
- A record of a login removed without `forget` is dropped without contacting the server, because
  its credentials are gone. The server deletes the subscription when it expires.

## Checks

`PushSubscriptionsTest` covers creation, verification, replacement of an earlier endpoint and
this device's old subscriptions, refusal, deletion, and an unreachable server.
`PushSubscriptionUpkeepTest` covers renewal, retried verification, http endpoints, and `forget`.
