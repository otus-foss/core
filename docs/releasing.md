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

Forgejo is the selected Git and package host. The Forgejo `publish` workflow uploads to
`https://git.hilfikers.com/api/packages/florian.ruby/maven` using
`publishAllPublicationsToForgejoPackagesRepository`. Configure its `FORGEJO_PACKAGES_TOKEN`
secret with a Forgejo `write:package` token and provide an `otus-macos` runner with Xcode 26.3.
Authenticated publication and downloads remain unverified. See [hosting and CI](hosting.md).

Publishing works like this:

1. Merge the changelog entry and version bump in `gradle.properties` to `main`.
2. Push the tag `v<version>`.
3. The `publish` workflow runs `checkAll`, builds every publication locally, then uploads every
   module to Forgejo Packages from a macOS runner, so the Android, iOS device, and iOS simulator
   variants come from one build.
4. Open a pull request in each app that bumps `otus-meta` in its version catalog when the app
   needs the release. Apps may stay on an older version.

Published versions are immutable. Fix a bad release with a new patch version.

## Migrating an existing release

Git migration does not copy published packages. Copy the original complete artifacts to Forgejo,
or publish from a clean checkout with the same library sources as the released tag. A changed
library must use a new version. Do not overwrite any existing package version.

The clean hosting checkout retains the library sources and version catalog from `v0.3.0` and
adds the Forgejo publication repository. Before publishing that version, verify the source match:

```sh
git diff --exit-code v0.3.0 -- accounts avatar dateformat designsystem jmap settings gradle gradle.properties settings.gradle.kts
```

On a Mac, configure these credentials in private `~/.gradle/gradle.properties`:

```properties
ForgejoPackagesUsername=florian.ruby
ForgejoPackagesPassword=<Forgejo token with write:package>
```

Verify that version 0.3.0 is absent from Forgejo, then build and publish the complete release:

```sh
./gradlew checkAll iosSimulatorArm64Test --console=plain
./gradlew publishToMavenLocal --console=plain
./gradlew publishAllPublicationsToForgejoPackagesRepository --console=plain
```

Run these from the verified clean checkout, not an in-progress library branch. The publication
includes all six modules' root, Android, iOS device, and iOS simulator artifacts. Do not upload
only the Android publications. Publishing to local Maven alone does not upload to Forgejo.
After uploading, run Mail against the remote registry and verify Android and iOS linking before
marking migration complete. Existing tags predate the Forgejo workflow; pushing an already-present
tag does not start a new release run.

## Compatibility checks

Before releasing a breaking change, build both apps against the release candidate through the
composite build described in [consuming.md](consuming.md). Record which app versions were checked
in the pull request.
