# Library architecture

## Ownership

This repository owns code that Otus Mail and Otus Calendar both use and that has no reason to
differ between them. Each app keeps its own app-specific features and screens, app wiring, navigation graph,
database instance, credentials, notifications, and release. Shared code does not give one app
access to the other app's accounts or storage.

Move code here when the second app needs it or the user explicitly requests a suite extraction
of existing behavior. The settings/account extraction followed that request. Calendar now consumes
the shared settings module; account setup remains app-specific there. The contacts modules began
as requested suite code and now serve Mail; see [decision 0016](decisions/0016-contacts-modules.md).
App-specific workflows and speculative modules stay out of this repository. All branded visual
elements, reusable components, visual tokens, and symbols live here, including those currently
used only by Mail. See [decision 0008](decisions/0008-central-ui-ownership.md).

## Modules

Each top-level directory with a `build.gradle.kts` and `src/` is one library module. A module
named `jmap-mail` uses the package `com.otusfoss.core.jmap.mail` and the coordinates `com.otusfoss.core:jmap-mail`,
which the apps' composite builds resolve to the module. The architecture checker derives the namespace from the directory name.

```text
<module>/
  build.gradle.kts
  src/
    commonMain/kotlin/com/otusfoss/core/<module>/   Portable code and public API
    androidMain/kotlin/com/otusfoss/core/<module>/  Android implementations
    iosMain/kotlin/com/otusfoss/core/<module>/      iOS implementations
    commonTest/kotlin/com/otusfoss/core/<module>/   Behavior tests
```

The repeated `src/<sourceSet>/kotlin` paths tell Kotlin which code each target compiles. Shared
code belongs in `commonMain`; Android and iOS implementations belong in their platform source
sets. Keep this build structure and organize responsibilities beneath the module's package.

### Extracted libraries

The first modules move existing code. `jmap` moved in version 0.1.0; `designsystem`, `dateformat`,
and `avatar` moved from Mail in 0.2.0. Version 0.3.0 added `accounts` and `settings`,
plus the historical date-range picker and avatar-stack pill in `designsystem`. Mail consumes every
module. Calendar consumes only `designsystem` so far. Both apps build Core from source; see
[consuming](consuming.md).

| Order | Module | Source | Notes |
| --- | --- | --- | --- |
| 1 | `jmap` | `mail/jmap` | Moved in 0.1.0. See [JMAP](jmap.md). |
| 2 | `designsystem` | `designsystem/` in both apps | Moved from Mail in 0.2.0. See [design system](designsystem.md). |
| 3 | `dateformat` | `core/dateformat` in both apps | Moved from Mail in 0.2.0. See [date formatting](dateformat.md). |
| 4 | `avatar` | `core/avatar` in both apps | Moved from Mail in 0.2.0. See [avatars](avatar.md). |
| 5 | `accounts` | Mail account setup | Moved in 0.3.0. See decision 0007. |
| 6 | `settings` | Mail settings | Moved in 0.3.0; both apps consume it. See decision 0007. |
| 7 | `jmap-push` | New | JMAP push subscriptions for Mail, and Calendar later. See [push](push.md) and decision 0015. |
| 8 | `jscontact` | New | JSContact cards (RFC 9553), consumed by Mail. Depends only on `json-types`. See [contacts](contacts.md) and decision 0016. |
| 9 | `jmap-contacts` | New | JMAP for Contacts (RFC 9610) on `jmap` and `jscontact`, consumed by Mail. See [contacts](contacts.md) and decision 0016. |
| 10 | `json-types` | `jmap` and `jscontact` | RFC 3339 dates and PatchObject paths, the RFC 8620 conventions RFC 9553 reuses; only kotlinx.serialization, so `jscontact` needs no networking. See [JMAP](jmap.md). |
| 11 | `storage` | Mail `core/preferences`, the four `*PreferenceStore`s, the stored-name digest | Key-value and string-map stores with Android and iOS implementations, `StoredChoice`, and `storedName`. See [shared features](shared-features.md#stored-choices). |
| 12 | `avatar-ui` | `accounts` presentation | Avatar looks and the own-avatar editor, so `accounts` needs no `avatar`. See [avatars](avatar.md). |

Later modules come from the roadmap issues once their APIs exist in one app and the second app
needs them. Candidates are storage (SQLDelight drivers, migrations, blob files), sync cursors,
the durable operation queue, filter definitions, invitation parsing, settings transfer, and
more per-capability JMAP modules such as `jmap-mail` or `jmap-calendars`, alongside
`jmap-contacts`. Split a
module on a real dependency difference, for example when Calendar must not pull in Sieve code.

## Dependency rules

The checker in `tools/check_architecture.py` enforces these rules at source level:

- No module references `com.otusfoss.mail` or `com.otusfoss.calendar`, including in tests.
- Packages match the source path and start with the module namespace.
- `commonMain` uses no Android, JVM, or Apple platform APIs.
- `designsystem` modules use `shared/<group>/elements` and `components` for shared UI, and
  `mail/<feature>/elements` and `components` for Mail rendering. Future Calendar rendering uses
  `calendar/<feature>` with the same layers. Elements never use components, including those in
  other groups. Shared UI cannot depend on Mail or Calendar UI groups, and the two app-specific
  groups cannot depend on each other. Theme code lives under the root `theme` subtree, with
  subpackages for colors, motion, branding, typography, and token families. None may depend on
  these UI groups. The module may format dates through `dateformat`, but cannot depend on account or
  settings features, protocol clients, networking, or storage. See
  [decision 0018](decisions/0018-ui-domain-packages.md).
- The shared `accounts`, `avatar-ui`, and `settings` features may use Compose in `presentation`
  only.
  Composables live in `presentation/components` or `presentation/screens`, with screen files
  named `*Screen.kt`. Domain cannot depend on data, presentation, or the design system.
  Presentation uses domain contracts, never data implementations or protocol clients.
- Every element and component takes values and callbacks. ViewModels, repositories, networking,
  storage, and navigation controllers stay outside them. Shared features do not import one
  another; app wiring composes them. Literal colors stay in the design-system `theme` subtree.
- `domain` packages use no UI, platform, networking, or database APIs.
- Animations take their duration and curve from `Motion`. Outside `theme/motion`, code never calls
  `spring` or `CubicBezierEasing`, and every `tween` or `TweenSpec` names its easing and takes no
  number literal as its duration. The checker cannot see a Compose default spec left out of a call
  such as `animateFloatAsState(target)` or `fadeIn()`, so review still covers those.
- No catch-all `utils`, `helpers`, or `manager` packages, wildcard imports, or inline
  suppressions.

Every module enables Kotlin explicit API mode and treats warnings as errors. Public declarations
are the contract with both apps, so changing them follows [the release rules](releasing.md).

Libraries use Ktor, kotlinx.serialization, kotlinx.coroutines, and kotlinx-datetime at the versions
pinned in `gradle/libs.versions.toml`, which the apps load as their catalog too, so one version
serves all three repositories.

## What else lives here

- `docs/suite/` holds the product goals and the mail design palette. These used to sit in the
  unversioned parent workspace. App repositories link here rather than keeping copies.
- `docs/decisions/` holds decisions that apply to the whole suite, starting with the file-length,
  Gradle-entry-point, portable-import, and commit-message rules that both apps already follow.
- `build-logic/`, `tools/`, and `config/` hold the convention plugins, checkers, and detekt
  configuration every repository runs; see [the quality guide](quality.md#build-logic).
- `.github/actions/` holds the CI steps the apps use from their core checkout; see
  [hosting and CI](hosting.md#shared-actions).

## Shared feature modules

`accounts` owns JMAP account contracts, the verifying repository, screen state, connected-account
rows, and the setup screen. The app supplies a login store, transport, coroutine scope, refresh
callbacks, the required `AccountService`, the `LoginLifecycle`s that follow each login, and the
avatars the setup screen shows (`AccountAvatars` and an editor slot). Mail defines its service
with the RFC 8621 mail capability and `Mailbox/get`; the library assumes no service.

`avatar-ui` owns how avatars look on the design system and the own-avatar editor. It takes each
account as an `OwnAccount` value, so it does not depend on `accounts`.

`settings` owns the settings list and its search, Appearance screen, date-format state, and
examples. The app builds a `settingsIndex` of its pages and settings, each page with a callback
that opens it on a found setting, and supplies the `StoredChoice` of each choice and navigation.
A page's rows can be declared once as `SettingsSectionSpec` values that both its screen
(`SettingSections`) and the index (`sections`) read.
Shared settings do not embed Mail folder or rule destinations.

Both modules retain `domain`, `data`, and `presentation` boundaries where used. Their screens
compose shared design-system components. App-specific navigation and installed-app credentials
remain independent. See [decision 0007](decisions/0007-shared-feature-modules.md).

## Central visual library

The package layout groups rendering by ownership, responsibility, and UI layer:

```text
com/otusfoss/core/designsystem/
  theme/OtusTheme.kt
  theme/colors/
  theme/motion/
  theme/brand/
  theme/typography/
  theme/tokens/<family>/
  shared/<group>/elements/
  shared/<group>/components/
  mail/<feature>/elements/
  mail/<feature>/components/
  mail/<feature>/html/          Plain Kotlin beside a group's UI; no composables
```

Groups such as `people`, `contacts`, `dates`, `rows`, `filters`, and `sidebar` hold shared UI.
Mail owns rendering groups such as `inbox`, `compose`, `read`, `folders`, and `attachments`.
Only create a layer directory when it contains code. Platform implementations and tests mirror
these paths. Keep component-specific display values and visual state beside their component;
app business models and services do not move into these groups. See the
[package migration guide](ui-package-migration.md) for the declaration mapping.

Token families group related visual rules, such as layout, rows, overlays, and editing, on top of
the core scales `Spacing`, `Sizes`, and `TypeScale`. Shared UI reads the scales and its own token
objects, never a Mail screen's (`InboxTokens`, `ComposeTokens`, `ReadTokens`); see
[the design system guide](designsystem.md#tokens).

`designsystem` also owns the public `com.otusfoss.core.designsystem.resources.Res` symbol bundle. Mail supplies display values and actions
through feature adapters. No Mail domain model enters these component APIs.
