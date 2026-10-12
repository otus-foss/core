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

**Publishing is paused.** A version tag no longer starts the `publish` workflow, so pushing a
tag uploads nothing. Manual runs still validate without uploading. Resume the tag trigger in
`.github/workflows/publish.yml` before the next release.

The GitHub `publish` workflow uploads to GitHub Packages at
`https://maven.pkg.github.com/reazndev/otus-meta` using
`publishAllPublicationsToGitHubPackagesRepository` and the workflow's `GITHUB_TOKEN`. It runs on
Ubicloud's `ubicloud-standard-2` Linux runner. See [hosting and CI](hosting.md).

Publishing works like this:

1. Merge the changelog entry and version bump in `gradle.properties` to `main`.
2. Push the tag `v<version>`. The pre-push hook runs `checkAll` first.
3. The `publish` workflow builds every publication in an isolated local Maven
   repository on Linux. `tools/check_publications.py` verifies all root, Android, iOS device, and
   iOS simulator publications, including the files and checksums in their Gradle metadata.
   Only then does the workflow upload every module to GitHub Packages.
4. Open a pull request in each app that bumps `otus-meta` in its version catalog when the app
   needs the release. Apps may stay on an older version.

Published versions are immutable. Fix a bad release with a new patch version.

To test the publisher without a release, select `publish` in GitHub Actions and use **Run workflow**
on the branch to validate. Manual runs never upload. A branch push does not publish; only a new
version tag starts an upload. A tag runs the workflow file from its own commit, so pushing a tag
from before the switch to GitHub starts that older workflow.

The current modules cross-compile their iOS Kotlin libraries on Linux. Final iOS binaries and
simulator tests require macOS and remain in the separate quality workflow, where `if: false`
currently pauses them. Linux publication does not establish iOS runtime validation. Record unrun
checks when releasing. A future custom cinterop or CocoaPods dependency may require a Mac to
build a complete publication; the artifact check must fail rather than allow an incomplete
release. See [decision 0011](decisions/0011-linux-library-publication.md).

## Compatibility checks

Before releasing a breaking change, build both apps against the release candidate through the
composite build described in [consuming.md](consuming.md). Record which app versions were checked
in the pull request.
