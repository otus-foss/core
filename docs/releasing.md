# Releasing

## Versions

All modules share one version and are released together from one tag. The apps do not consume
releases: they build Meta's `main` from source, see [consuming](consuming.md), so a version number
protects no caller. A release only names a point in Meta's history that the changelog describes;
raise the minor number for each one.

## Changelog

Every pull request that changes a module's public API or behavior adds a line under
`## Unreleased` in `CHANGELOG.md`. A breaking change names the affected declarations and what
the apps change, which their partner branches do in the same landing. A release renames
`Unreleased` to the version and date.

## Tags

Meta builds no packages; see [decision 0019](decisions/0019-no-library-publishing.md). A release sets
`version` in `gradle.properties`, renames `Unreleased` in the changelog, and tags that commit on
`main` as `v<version>`. Run `./gradlew checkAll` on that commit first; no
hook runs it for a tag.

iOS library variants compile on Linux, but final iOS binaries and simulator tests need macOS and
run only in the quality workflow's `ios` job while the `IOS_CI` repository variable is `on`.
Record unrun checks when tagging.

## Compatibility checks

Both apps build against Meta's source, so a breaking change lands with the matching app changes.
Open them on branches with the same name. Meta's `quality` workflow compiles each app at that
branch, or at its `main`, against the Meta pull request, so a change that breaks an app fails
there first; see [consuming.md](consuming.md).
