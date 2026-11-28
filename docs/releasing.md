# Releasing

## Versions

All modules share one version and are released together from one tag. The apps do not consume
releases: they build Core's `main` from source, see [consuming](consuming.md), so a version number
protects no caller. A release only names a point in Core's history that the changelog describes;
raise the minor number for each one.

## Changelog

Every pull request that changes a module's public API or behavior adds a fragment to
`changelog.d/`: a Markdown file named after the branch, with `/` as `-`, holding one bullet per
change. A breaking change names the affected declarations and what the apps change, which their
partner branches do in the same landing. Pull requests never edit `CHANGELOG.md`, because they
all add to the top of one list and each merge would leave the others conflicting.

A release runs `python3 tools/changelog.py <version>`. It puts the fragments above the entries
under `## Unreleased`, names them `## <version> - <date>`, leaves an empty `## Unreleased` on
top, and deletes the fragments.

## Tags

Core builds no packages; see [decision 0020](decisions/0020-no-library-publishing.md). A release sets
`version` in `gradle.properties`, runs `tools/changelog.py` with that version, and tags that
commit on `main` as `v<version>`. Run `./gradlew checkAll` on that commit first; no
hook runs it for a tag.

iOS library variants compile on Linux, but final iOS binaries and simulator tests need macOS and
run only in the quality workflow's `ios` job while the `IOS_CI` repository variable is `on`.
Record unrun checks when tagging.

## Compatibility checks

Both apps build against Core's source, so a breaking change lands with the matching app changes.
Open them on branches with the same name. Core's `quality` workflow compiles each app at that
branch, or at its `main`, against the Core pull request, so a change that breaks an app fails
there first; see [consuming.md](consuming.md).
