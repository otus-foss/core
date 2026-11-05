# Releasing

## Versions

All modules share one version and are released together from one tag. The apps do not consume
releases: they build Meta from source, see [consuming](consuming.md). A release gives a fixed
version to anyone outside the suite who needs one.

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
2. Run `./gradlew checkAll` on a commit that is already on `main`, then push the tag
   `v<version>` on it. No hook runs `checkAll`, and the workflow below skips part of it.
3. The `publish` workflow's `build` job checks that the tag matches `gradle.properties` and that
   the tagged commit is an ancestor of `main`, so a tag on an unmerged branch never publishes.
   It runs `./gradlew testAndroidHostTest`, which covers every module's common and Android host
   tests, the part of `checkAll` that exercises library behavior on Linux. It skips the formatting
   and static checks that the hooks already ran. It then builds every publication in an isolated
   local Maven repository on Linux. `tools/check_publications.py` verifies all root, Android, iOS
   device, and iOS simulator publications, the files and checksums in their Gradle metadata, and
   that every `com.otus` dependency in each POM and Gradle module file names the release version.
4. Only after the `build` job passes does the `publish` job upload every module to GitHub
   Packages with Gradle.

Published versions are immutable. Fix a bad release with a new patch version.

Each job gets only the permissions it needs. The `build` job can read the repository and nothing
else, so tests and build scripts never run with a token that can publish. Only the `publish` job
can write packages. Neither checkout keeps Git credentials. The `publish` job builds the
publications again from the same commit instead of uploading the `build` job's files, because
uploading a Maven repository without Gradle would bypass the metadata handling that Gradle's
publisher does against GitHub Packages, and that path cannot be tested without a release.

To test the publisher without a release, select `publish` in GitHub Actions and use **Run workflow**
on the branch to validate. Manual runs never upload. A branch push does not publish; only a new
version tag starts an upload. A tag runs the workflow file from its own commit, so pushing a tag
from before the switch to GitHub starts that older workflow.

The current modules cross-compile their iOS Kotlin libraries on Linux. Final iOS binaries and
simulator tests require macOS and remain in the separate quality workflow's `ios` job, which runs
only while the `IOS_CI` repository variable is `on`. Linux publication does not establish iOS runtime validation. Record unrun
checks when releasing. A future custom cinterop or CocoaPods dependency may require a Mac to
build a complete publication; the artifact check must fail rather than allow an incomplete
release. See [decision 0011](decisions/0011-linux-library-publication.md).

## Compatibility checks

Both apps build against Meta's source, so a breaking change lands with the matching app changes:
open them on branches with the same name and merge Meta's first; see
[consuming.md](consuming.md).
