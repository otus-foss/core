# Otus Meta

Shared Kotlin Multiplatform libraries, suite documentation, and engineering policy for the Otus
JMAP suite. The two apps live in their own repositories and consume released versions of these
libraries:

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
| [`jmap`](docs/jmap.md) | `com.otus:jmap` | JMAP client (RFC 8620) and protected login storage |
| [`dateformat`](docs/dateformat.md) | `com.otus:dateformat` | Date display and the date-format preference |
| [`avatar`](docs/avatar.md) | `com.otus:avatar` | Contact photos, website icons, and generated initials |
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
| `docs/releasing.md` | Versioning, changelog, and publishing rules |
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
