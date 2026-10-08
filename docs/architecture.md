# Library architecture

## Ownership

This repository owns code that Otus Mail and Otus Calendar both use and that has no reason to
differ between them. Each app keeps its own features, screens, app wiring, navigation graph,
database instance, credentials, notifications, and release. Shared code does not give one app
access to the other app's accounts or storage.

Move code here when the second app needs it, not in anticipation. Code that only one app uses
stays in that app, even if it might be reused later.

## Modules

Each top-level directory with a `build.gradle.kts` and `src/` is one library module. A module
named `jmap-mail` uses the package `com.otus.jmap.mail` and publishes the Maven artifact
`com.otus:jmap-mail`. The architecture checker derives the namespace from the directory name.

```text
<module>/
  build.gradle.kts
  src/
    commonMain/kotlin/com/otus/<module>/   Portable code and public API
    androidMain/kotlin/com/otus/<module>/  Android implementations
    iosMain/kotlin/com/otus/<module>/      iOS implementations
    commonTest/kotlin/com/otus/<module>/   Behavior tests
```

### Planned extraction

The first modules move existing code. `jmap` moved in version 0.1.0; `designsystem`, `dateformat`,
and `avatar` moved from Mail in 0.2.0. Calendar still carries older copies of the last three until
it adopts the modules.

| Order | Module | Source | Notes |
| --- | --- | --- | --- |
| 1 | `jmap` | `otus-mail/jmap` | Moved in 0.1.0. See [JMAP](jmap.md). |
| 2 | `designsystem` | `designsystem/` in both apps | Moved from Mail in 0.2.0. See [design system](designsystem.md). |
| 3 | `dateformat` | `core/dateformat` in both apps | Moved from Mail in 0.2.0. See [date formatting](dateformat.md). |
| 4 | `avatar` | `core/avatar` in both apps | Moved from Mail in 0.2.0; Calendar's copy is older. See [avatars](avatar.md). |

Later modules come from the roadmap issues once their APIs exist in one app and the second app
needs them. Candidates are storage (SQLDelight drivers, migrations, blob files), sync cursors,
the durable operation queue, contacts, filter definitions, invitation parsing, settings
transfer, and per-capability JMAP modules such as `jmap-mail` or `jmap-calendars`. Split a
module on a real dependency difference, for example when Calendar must not pull in Sieve code.

## Dependency rules

The checker in `tools/check_architecture.py` enforces these rules at source level:

- No module references `com.otus.mail` or `com.otus.calendar`, including in tests.
- Packages match the source path and start with the module namespace.
- `commonMain` uses no Android, JVM, or Apple platform APIs.
- Only `designsystem` modules use Compose or lifecycle APIs. Their code lives in `elements`,
  `components`, or `theme`. Elements do not use components. Literal colors stay in `theme`.
- Design-system code takes values and callbacks. It does not use protocol, data, networking, or
  database modules, ViewModels, repositories, or navigation controllers.
- `domain` packages use no UI, platform, networking, or database APIs.
- No catch-all `utils`, `helpers`, or `manager` packages, wildcard imports, or inline
  suppressions.

Every module enables Kotlin explicit API mode and treats warnings as errors. Public declarations
are the contract with both apps, so changing them follows [the release rules](releasing.md).

Libraries use Ktor, kotlinx.serialization, kotlinx.coroutines, and kotlinx-datetime at the versions
pinned in `gradle/libs.versions.toml`. Keep that catalog aligned with the apps. A consuming app
must compile with a Kotlin version at least as new as the one used to publish the library.

## What else lives here

- `docs/suite/` holds the product goals and the mail design palette. These used to sit in the
  unversioned parent workspace. App repositories link here rather than keeping copies.
- `docs/decisions/` holds decisions that apply to the whole suite, starting with the file-length,
  Gradle-entry-point, portable-import, and commit-message rules that both apps already follow.
- `tools/` is the reference copy of the quality checkers. The apps keep their own copies so that
  each clone builds alone. Change the reference copy first, then update the apps.
