# Releasing

## Versions

All modules share one version and are released together from one tag. The apps do not consume
releases: they build Meta from source, see [consuming](consuming.md). A release names a point in
Meta's history that the changelog describes.

Use semantic versioning. While the version is below 1.0.0, a minor release (`0.2.0`) may break
public API and a patch release (`0.1.1`) may not. From 1.0.0, only a major release may break
public API.

## Changelog

Every pull request that changes a module's public API or behavior adds a line under
`## Unreleased` in `CHANGELOG.md`. A breaking change names the affected declarations and the
migration each app needs. A release renames `Unreleased` to the version and date.

## Tags

Meta builds no packages; see [decision 0019](decisions/0019-no-library-publishing.md). A release sets
`version` in `gradle.properties`, renames `Unreleased` in the changelog, and tags that commit on
`main` as `v<version>`. Run `./gradlew checkAll` on that commit first; no
hook runs it for a tag.

iOS library variants compile on Linux, but final iOS binaries and simulator tests need macOS and
run only in the quality workflow's `ios` job while the `IOS_CI` repository variable is `on`.
Record unrun checks when tagging.

## Compatibility checks

Both apps build against Meta's source, so a breaking change lands with the matching app changes:
open them on branches with the same name and merge Meta's first; see
[consuming.md](consuming.md).
