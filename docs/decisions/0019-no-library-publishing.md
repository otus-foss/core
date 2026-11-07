# 0019: Meta publishes no artifacts

Status: follows the user's decision of 2026-11-06 that both apps build Meta from source.

Supersedes the publishing parts of [decision 0005](0005-shared-library-repository.md) and
[decision 0011](0011-linux-library-publication.md).

## Problem

Since the 2026-11-06 amendment to decision 0005, Mail and Calendar include an otus-meta checkout
as a composite build and pin no version. The `publish` workflow had been paused since before
then: a tag uploaded nothing and a manual run only validated. Nothing outside the suite consumes
the packages, and the repositories are private. The workflow, `tools/check_publications.py`, the
`maven-publish` setup in the library convention plugins, and the GitHub Packages repository in
`OtusLibrary.kt` were kept working for no consumer.

## Decision

Meta builds no publications. The `publish` workflow, the publication checker and its tests, and
the `maven-publish` plugin are removed. Modules keep the `com.otus:<module>` coordinates, because
the apps' composite builds substitute those coordinates with Meta's projects. The `GITHUB_TOKEN`
exception in the workflow secret check goes with the only step that used it.

Versions and the changelog stay. A release is a `v<version>` tag on `main` that names a point in
Meta's history; see [releasing](../releasing.md).

## Consequences

- No CI job builds Meta's iOS library variants any more. In their place, the quality workflow's
  `ios metadata` part compiles every module's `iosMain` on Linux, and the `ios` job runs the iOS
  tests on macOS when enabled.
- The packages already on GitHub Packages (up to 0.3.0, and from 0.6.0 on) stay there and get
  no new versions.
- Publishing again means restoring the workflow and checker from the commit that removed them
  and adding `maven-publish` back to `otus.kmp-library` and `otus.kmp-compose-library`.
