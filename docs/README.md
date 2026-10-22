# Otus Meta documentation

## Suite

- [Hosting and CI](hosting.md): GitHub repositories, runners, and packages.

- [Product goals](suite/product-goals.md): purpose, protocol scope, and current state of both apps.
- [Mail design foundations](suite/mail-design.md): the palette and Figma reference used by both apps.
- [JMAP features in Otus](suite/jmap-features.md): which capabilities and methods the apps use,
  build, plan, or leave out. Read it before designing a screen or setting.
- [Roadmap](https://github.com/reazndev/otus-mail/issues/9): the v1 JMAP suite and v2 protocol plan.

## Libraries

- [Architecture](architecture.md): module ownership, planned extraction, and dependency rules.
- [JMAP](jmap.md): the `jmap` module's API, security rules, protocol coverage, and checks.
- [Contacts](contacts.md): the `jscontact` and `jmap-contacts` modules for JSContact cards and
  JMAP for Contacts, with their RFC coverage. No app uses them yet.
- [Push subscriptions](push.md): the `push` module that keeps JMAP push subscriptions current.
- [Date formatting](dateformat.md): the `dateformat` module's choices, API, and storage.
- [Avatars](avatar.md): the `avatar` module's lookup order, caching, and privacy limits.
- [Shared features](shared-features.md): settings and JMAP account setup modules.
- [Design system](designsystem.md): the `designsystem` module's contents, resources, and tests.
- [Using the libraries from an app](consuming.md): GitHub Packages, credentials, and local builds.
- [Releasing](releasing.md): versions, changelog, and publishing.
- [Quality gate](quality.md): commands, checks, and file-length exceptions.

## Decisions

- [0001: File-length exceptions](decisions/0001-file-length-exceptions.md)
- [0002: Gradle quality commands without npm](decisions/0002-gradle-quality-entry-points.md)
- [0003: Portable Compose imports](decisions/0003-portable-compose-imports.md)
- [0004: Commit messages](decisions/0004-commit-messages.md)
- [0005: Shared libraries in otus-meta](decisions/0005-shared-library-repository.md)
- [0006: Warnings in the design system's iOS metadata](decisions/0006-native-metadata-warnings.md)

- [0007: Shared feature modules](decisions/0007-shared-feature-modules.md)
- [0008: Central UI ownership](decisions/0008-central-ui-ownership.md)
- [0009: Forgejo primary Git host](decisions/0009-forgejo-primary.md), replaced by 0012
- [0011: Complete library publication from Linux](decisions/0011-linux-library-publication.md)
- [0012: GitHub as the primary host again](decisions/0012-github-primary.md)
- [0013: Local hooks as the quality gate](decisions/0013-local-quality-gate.md)
- [0014: User themes and accent colors](decisions/0014-user-themes.md)
- [0015: Push notifications without an Otus server](decisions/0015-push-notifications.md)
- [0016: JMAP contacts modules](decisions/0016-contacts-modules.md)

Decisions 0001 to 0004 were made in the app repositories and apply to every Otus repository.
