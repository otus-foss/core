# 0011: Complete library publication from Linux

Status: accepted following the user's Linux-only Forgejo runner configuration on 2026-10-09.
Since [decision 0012](0012-github-primary.md), the same Linux publisher runs on GitHub and uploads
to GitHub Packages. Superseded in part by [decision 0013](0013-local-quality-gate.md): the
publisher no longer runs `checkAll`. It runs the modules' host tests instead; see
[releasing](../releasing.md).

## Problem

Forgejo has an `ubuntu-latest` runner and no macOS runner. The original publisher required
`otus-macos`, so it could not start. Publishing only Android artifacts would leave root Maven
metadata pointing to missing iOS libraries.

## Decision

Run Forgejo's publisher on Linux. The pinned Kotlin 2.4.10 toolchain can compile all six modules'
Android artifacts and both iOS Kotlin library variants there. A local build without the build
cache produced all 24 root and platform publications. This compiles Kotlin libraries, not final
iOS applications. See [Kotlin's host requirements](https://kotlinlang.org/docs/multiplatform/multiplatform-publish-lib-setup.html#host-requirements).

Run `checkAll`, build the complete release into an isolated local Maven repository, then verify
the root metadata's platform references and every platform's binary, POM, Gradle metadata,
and referenced artifact checksums before uploading. Missing iOS artifacts must fail the release.
A manual workflow run performs these checks without uploading. Version tags upload to Forgejo.
The Forgejo Android quality job also verifies local publications on pull requests and main pushes,
so incomplete releases fail before a version tag is created.

Move simulator execution out of the Forgejo publication job. Keep the separate macOS iOS quality
job and GitHub's macOS workflow. The instance cannot execute its macOS job until a runner is
available; report this validation gap when releasing. No Linux check proves iOS linking or
runtime behavior. This explicitly changes the publisher's simulator-test gate because the
required execution environment is unavailable, while preserving the test tasks and iOS sources.

## Consequences

- Forgejo package creation no longer needs a macOS runner for the current modules.
- All six modules retain Android, iOS device, and iOS simulator publications.
- Existing versions remain immutable. Manual validation never republishes version 0.3.0.
- Future custom cinterop or CocoaPods dependencies can require macOS. Check that a complete
  Linux publication still builds before adopting them; never bypass the publication verifier.
- iOS linking and simulator validation remain outstanding until a macOS environment runs them.
