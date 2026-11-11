![Otus](.github/banner.png)

Otus Core holds the Kotlin Multiplatform libraries that [Otus Mail](https://github.com/otus-foss/mail)
and [Otus Calendar](https://github.com/otus-foss/calendar) share: the JMAP client, contacts, push,
the design system, and suite services such as dates and avatars. It also holds the suite's product
and design documents and the architecture decisions for every repository.

> [!WARNING]
> Otus Core is still in development. Expect missing features and APIs that change between
> commits.

## Install

Core is not an app, and there is nothing to install. Each app builds Core from a checkout next to
its own, so clone all three side by side:

```sh
git clone https://github.com/otus-foss/core
git clone https://github.com/otus-foss/mail
git clone https://github.com/otus-foss/calendar
```

See [Using Core from an app](docs/consuming.md) for other layouts.

## Development

Development, pull requests, and issues use
[core on GitHub](https://github.com/otus-foss/core). See [hosting and CI](docs/hosting.md).

Shared Kotlin Multiplatform libraries, suite documentation, and engineering policy for the Otus
JMAP suite. The two apps live in their own repositories and build these libraries from a Core
checkout:

- [Otus Mail](https://github.com/otus-foss/mail)
- [Otus Calendar](https://github.com/otus-foss/calendar)

This repository holds code that both apps need: the JMAP client, the design system, and portable
suite services such as date formatting and avatars. It also holds suite-wide product and design
documents and the architecture decisions that apply to every repository. App-specific screens, feature
wiring, navigation, databases, and credentials stay in each app. Shared settings and account
setup screens receive app-owned state and callbacks.

### Status

| Module | Artifact | Contents |
| --- | --- | --- |
| [`json-types`](docs/jmap.md) | `com.otusfoss.core:json-types` | RFC 3339 dates and PatchObject paths that JMAP and JSContact share |
| [`storage`](docs/shared-features.md#stored-choices) | `com.otusfoss.core:storage` | Key-value and string-map stores, stored choices, and stored names |
| [`jmap`](docs/jmap.md) | `com.otusfoss.core:jmap` | JMAP client (RFC 8620), account lookup, login lifecycle, and protected login storage |
| [`jscontact`](docs/contacts.md) | `com.otusfoss.core:jscontact` | JSContact cards (RFC 9553) |
| [`jmap-contacts`](docs/contacts.md) | `com.otusfoss.core:jmap-contacts` | JMAP for Contacts (RFC 9610) |
| [`jmap-push`](docs/push.md) | `com.otusfoss.core:jmap-push` | JMAP push subscriptions |
| [`dateformat`](docs/dateformat.md) | `com.otusfoss.core:dateformat` | Date display, with day, month, and time names |
| [`avatar`](docs/avatar.md) | `com.otusfoss.core:avatar` | Contact photos, website icons, and generated initials |
| [`avatar-ui`](docs/avatar.md#avatar-ui) | `com.otusfoss.core:avatar-ui` | Avatar looks on the design system and the own-avatar editor |
| [`designsystem`](docs/designsystem.md) | `com.otusfoss.core:designsystem` | Theme, elements, and components |
| [`settings`](docs/shared-features.md) | `com.otusfoss.core:settings` | Settings and appearance pages, added in 0.3.0 |
| [`accounts`](docs/shared-features.md) | `com.otusfoss.core:accounts` | JMAP account setup, added in 0.3.0 |

More modules follow as code moves out of the apps. See
[the architecture guide](docs/architecture.md) for the planned modules and extraction order.

### Commands

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
