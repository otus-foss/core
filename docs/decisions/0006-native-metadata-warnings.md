# 0006: Warnings in the design system's iOS metadata

Status: accepted on 2026-10-08.

## Problem

Publishing compiles the design system's shared `iosMain` source set into Kotlin metadata. Compose
Multiplatform 1.11.1 brings in Google's `androidx.compose.runtime`, `androidx.lifecycle`, and
related packages next to JetBrains' own builds of them. The Kotlin library loader then warns that
the same `unique_name` appears in two libraries. With warnings as errors, publishing 0.2.0 failed
after `jmap`, `dateformat`, and `avatar` had already been uploaded.

The warning is about third-party dependencies, not this repository's source. No Otus dependency
declaration selects the duplicate packages.

## Decision

Turn off warnings as errors only for the design system's `compileIosMainKotlinMetadata` task. The
`iosArm64` and `iosSimulatorArm64` compilations of the same sources keep warnings as errors, so a
warning in Otus code still fails the build. Every other module and task is unchanged.

Remove the exception when Compose Multiplatform stops producing the duplicate libraries. Check by
deleting it and running `./gradlew :designsystem:compileIosMainKotlinMetadata`.

## Related changes

- The `android` CI job runs `publishToMavenLocal`, so publication problems appear before a tag.
- The publish workflow builds every publication locally before uploading any of them.
- Since [decision 0020](0020-no-library-publishing.md), Meta builds no publications, so neither
  check runs any more.
