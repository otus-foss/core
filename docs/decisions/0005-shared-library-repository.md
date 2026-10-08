# 0005: Shared libraries in otus-meta

Status: accepted by the user on 2026-10-08.

Hosting is amended by [decision 0009](0009-forgejo-primary.md). Forgejo is the primary Git
host; GitHub Packages remains the working registry until package migration is verified.

## Decision

Shared code for Otus Mail and Otus Calendar lives in a third repository, `reazndev/otus-meta`.
It publishes versioned Kotlin Multiplatform artifacts `com.otus:<module>` to GitHub Packages. The
apps pin a released version in their version catalogs. Developers can opt into a composite build
against a local checkout. CI never uses it.

The repository also holds suite documentation and decisions that previously lived in the
unversioned parent workspace, so a clone of any one repository no longer depends on files that
exist only on one machine.

This replaces the earlier rule in both app guides that library distribution was undecided and
that no third repository should be created. Roadmap issue otus-mail#10 proposed the name
`otus-core`. The user chose `otus-meta` because the repository holds more than code.

## Reasons

- Mail and Calendar already carry diverging copies of the theme, date formatting, and avatar code.
- Calendar needs Mail's JMAP client and will need the same storage, sync, contacts, and
  operation-queue code.
- Pinned versions let each app release on its own schedule and upgrade the libraries separately.

## Alternatives considered

- A Gradle module inside otus-mail that Calendar includes as a Git submodule. No publishing or
  tokens, but Calendar's build would depend on Mail's repository layout and history.
- Composite builds only, with sibling checkouts. Simple locally, but CI and fresh clones would
  need the sibling checkout, which the app guides forbid.
- Maven Central. Public artifacts with no download token, but the repositories are private and
  Central requires a verified group namespace.

## Consequences

- Downloading from GitHub Packages needs a token, including in each app's CI. See
  [consuming.md](../consuming.md).
- Public API changes need versioning, a changelog entry, and checks against both apps. See
  [releasing.md](../releasing.md).
- Publishing iOS variants needs a macOS runner.
- The `com.otus` group is not a verified namespace. Moving to Maven Central later requires a new
  group such as `io.github.reazndev`, which is a breaking coordinate change for both apps.
