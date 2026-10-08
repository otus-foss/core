# Releasing

## Versions

All modules share one version and are released together from one tag. An app pins a single
`otus-meta` version for every module it uses, so modules never mix versions inside an app.

Use semantic versioning. While the version is below 1.0.0, a minor release (`0.2.0`) may break
public API and a patch release (`0.1.1`) may not. From 1.0.0, only a major release may break
public API.

## Changelog

Every pull request that changes a module's public API or behavior adds a line under
`## Unreleased` in `CHANGELOG.md`. A breaking change names the affected declarations and the
migration each app needs. A release renames `Unreleased` to the version and date.

## Publishing

Publishing is set up together with the first module. The intended flow:

1. Merge the changelog entry and version bump in `gradle.properties` to `main`.
2. Push the tag `v<version>`.
3. The `publish` workflow runs `checkAll`, then publishes every module to GitHub Packages from a
   macOS runner so that the Android, iOS device, and iOS simulator variants come from one build.
4. Open a pull request in each app that bumps `otus-meta` in its version catalog when the app
   needs the release. Apps may stay on an older version.

Published versions are immutable. Fix a bad release with a new patch version.

## Compatibility checks

Before releasing a breaking change, build both apps against the release candidate through the
composite build described in [consuming.md](consuming.md). Record which app versions were checked
in the pull request.
