# Otus Meta

Development, pull requests, and issues use
[otus-meta on GitHub](https://github.com/reazndev/otus-meta). See [hosting and CI](docs/hosting.md).

Shared Kotlin Multiplatform libraries, suite documentation, and engineering policy for the Otus
JMAP suite. The two apps live in their own repositories and build these libraries from a Meta
checkout:

- [Otus Mail](https://github.com/reazndev/otus-mail)
- [Otus Calendar](https://github.com/reazndev/otus-calendar)

This repository holds code that both apps need: the JMAP client, the design system, and portable
suite services such as date formatting and avatars. It also holds suite-wide product and design
documents and the architecture decisions that apply to every repository. App-specific screens, feature
wiring, navigation, databases, and credentials stay in each app. Shared settings and account
setup screens receive app-owned state and callbacks.

## Status

| Module | Artifact | Contents |
| --- | --- | --- |
| [`json-types`](docs/jmap.md) | `com.otus:json-types` | RFC 3339 dates and PatchObject paths that JMAP and JSContact share |
| [`storage`](docs/shared-features.md#stored-choices) | `com.otus:storage` | Key-value and string-map stores, stored choices, and stored names |
| [`jmap`](docs/jmap.md) | `com.otus:jmap` | JMAP client (RFC 8620), account lookup, login lifecycle, and protected login storage |
| [`jscontact`](docs/contacts.md) | `com.otus:jscontact` | JSContact cards (RFC 9553) |
| [`jmap-contacts`](docs/contacts.md) | `com.otus:jmap-contacts` | JMAP for Contacts (RFC 9610) |
| [`jmap-push`](docs/push.md) | `com.otus:jmap-push` | JMAP push subscriptions |
| [`dateformat`](docs/dateformat.md) | `com.otus:dateformat` | Date display, with day, month, and time names |
| [`avatar`](docs/avatar.md) | `com.otus:avatar` | Contact photos, website icons, and generated initials |
| [`avatar-ui`](docs/avatar.md#avatar-ui) | `com.otus:avatar-ui` | Avatar looks on the design system and the own-avatar editor |
| [`designsystem`](docs/designsystem.md) | `com.otus:designsystem` | Theme, elements, and components |
| [`settings`](docs/shared-features.md) | `com.otus:settings` | Settings and appearance pages, added in 0.3.0 |
| [`accounts`](docs/shared-features.md) | `com.otus:accounts` | JMAP account setup, added in 0.3.0 |

More modules follow as code moves out of the apps. See
[the architecture guide](docs/architecture.md) for the planned modules and extraction order.

## Contents

| Path | Purpose |
| --- | --- |
| `<module>/` | One Kotlin Multiplatform library per directory, package `com.otus.<module>` |
| `docs/suite/` | Product goals and the mail design palette shared by both apps |
| `docs/decisions/` | Architecture decision records for the suite and this repository |
| `docs/consuming.md` | How the apps resolve these libraries in CI and during local development |
| `docs/releasing.md` | Versioning and changelog rules |
| `tools/` | Architecture, file-length, and commit-message checkers with regression tests |

## Commands

Requires JDK 21 and Python 3.11+. Android library modules also need the Android SDK.

```sh
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements-quality.txt
git config --local core.hooksPath .githooks
./gradlew formatAll
./gradlew checkAll
```

Read [AGENTS.md](AGENTS.md) and [the quality guide](docs/quality.md) before changing code.

## License

GPL-3.0-or-later, with an [additional permission](LICENSE-EXCEPTION.md) that allows app-store
distribution while the source stays public. See [LICENSE](LICENSE).
