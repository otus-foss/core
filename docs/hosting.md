# Hosting and CI

## Primary repositories

Forgejo became the primary Git host on 2026-10-08:

- [Otus Mail](https://git.hilfikers.com/florian.ruby/otus-mail)
- [Otus Calendar](https://git.hilfikers.com/florian.ruby/otus-calendar)
- [Otus Meta](https://git.hilfikers.com/florian.ruby/otus-meta)

Develop, open pull requests, and file new issues on Forgejo. Each remains an independent
repository. Keep historical GitHub issue links until imported issue numbers are verified.

Existing checkouts use `origin` for Forgejo and `github` for GitHub. To migrate another checkout,
run these commands from that repository, replacing `<repo>` with its name:

```sh
git remote rename origin github
git remote add origin git@git.hilfikers.com:florian.ruby/<repo>.git
git fetch origin
git config --local remote.pushDefault origin
git config --local core.hooksPath .githooks
git branch --set-upstream-to=origin/main main
```

Update other tracked branches to `origin/<branch>` after they exist on Forgejo. Pushes must
preserve existing history. Do not force-push or use `git push --mirror` to reconcile differences.

GitHub remains a secondary copy. A local `github` remote does not synchronize it automatically.
Until a server push mirror is configured, push the same accepted commit explicitly to each host:

```sh
git push origin main
git push github main
```

Respect each host's branch protection. If direct pushes are rejected, use its pull request flow.
Avoid making independent changes on GitHub. Pull requests, issues, workflow results, credentials,
and settings do not synchronize through Git. SSH access from a workstation also does not give
Forgejo a credential for pushing to GitHub.

## Workflow configuration

`.forgejo/workflows` contains the Forgejo workflows. `.github/workflows` keeps the existing
GitHub workflows so mirrored commits can still use GitHub CI and publishing. Forgejo selects
its own workflow directory instead of falling back to the GitHub files.

The GitHub workflows run their Linux jobs on Ubicloud's `ubicloud-standard-2` runners.
The GitHub `ios` quality job and the macOS GitHub Packages `publish` job are paused with
`if: false`; delete that condition to run them again.

The Forgejo workflows retain portable, Android, and iOS checks. Action URLs explicitly select
the pinned GitHub action commits. They use the Gradle wrapper directly and do not require an
Actions cache service. Neither iOS checks nor publishing have been verified on Forgejo yet.

Register Forgejo Runner 7 or newer and provide these labels and environments:

- `ubuntu-latest`: a Linux execution environment with Git, Bash, Node.js 20 for the pinned
  JavaScript actions, and the system libraries required by Temurin 21, Python 3.13, and the
  Android command-line tools. Actions install the JDK, Python, Android platform 37.0, and
  build-tools 36.0.0. Host Android tests also need the native graphics libraries used by
  Robolectric. Use a maintained runner image that provides those libraries.
- `otus-macos`: a macOS 15 Apple Silicon host with Git, Bash, Node.js 20, Homebrew, and Xcode
  26.3 at `/Applications/Xcode_26.3.app`. It must allow `sudo xcode-select`. Actions install
  Temurin 21 and Mint for the apps' pinned Swift tools.

The Linux label follows the instance maintainer's `runs-on: ubuntu-latest` guidance.
The `portable` and `android` jobs must match a runner available to each repository.
The `ios` quality jobs still require `otus-macos`; a Linux runner cannot run Xcode
or iOS simulator tests. No macOS runner is currently available on this Forgejo instance,
so these jobs cannot execute there. Keep their validation requirement visible.
Meta's publisher uses `ubuntu-latest` to compile the complete library publication, including
both iOS Kotlin libraries. This does not link an iOS application or run simulator tests.

The labels are requirements for runner registration, not a claim that those runners exist.
Enable Actions in each repository and require the resulting `portable`, `android`, and `ios`
statuses on `main` after their names and successful execution are verified. Keep existing
required checks intact; do not bypass them to complete the migration.

## Packages

Forgejo Packages is the selected release registry at
`https://git.hilfikers.com/api/packages/florian.ruby/maven`. Mail defaults to this registry in
its package migration change. Calendar does not yet consume Meta libraries. Version 0.3.1, with
the same library sources as 0.3.0, is the first release published there; 0.3.0 is not on
Forgejo. Moving Git repositories does not transfer Maven packages.

Meta's `publishAllPublicationsToForgejoPackagesRepository` uploads every module and platform
publication. Its Gradle credentials are `ForgejoPackagesUsername` and `ForgejoPackagesPassword`.
Create a Forgejo token with `write:package` and store it as `OTUS_META_PUBLISH_TOKEN` in Meta's
Forgejo Actions secrets. Forgejo rejects secret names that start with `FORGEJO_`, `GITEA_`, or
`GITHUB_`. The `publish` workflow runs on version tags using `ubuntu-latest`.
It runs `checkAll`, builds all publications in an isolated local Maven repository, and verifies
the root metadata, platform binaries, and metadata checksums before uploading. A manual run
performs the same validation without a publishing token or any upload. See
[releasing](releasing.md) for an existing version's migration.

For Mail's Actions settings on either CI host, configure:

- `OTUS_META_PACKAGES_TOKEN`: a Forgejo token with `read:package` and access to `florian.ruby`'s packages.
- `OTUS_META_PACKAGES_USERNAME`: the token's Forgejo username, defaulting to `florian.ruby`.
- `OTUS_META_REGISTRY_URL`: an optional registry override. Remove an old GitHub URL override.

Create these tokens under Forgejo user Settings → Applications. Package scopes cannot be used
with tokens restricted to specific repositories; select access that includes the package owner.
See [Forgejo token scopes](https://forgejo.org/docs/latest/user/authentication/token-scope/).
SSH keys do not authenticate Maven HTTP requests. Keep tokens in Actions secrets or private
Gradle properties. See [consuming](consuming.md) for local credentials.

Before marking the migration complete, verify all six root artifacts and their Android, iOS
device, and iOS simulator publications, then run a clean Mail Android build and iOS linking.
Preserve the released version's library sources and artifacts. Changed library code requires a
new version; do not upload only Android variants or overwrite an existing release.

## Unverified server settings

SSH Git access was tested for all three repositories. Web and API access require sign-in,
which was unavailable during the cutover. Runner availability, Actions enablement, secrets,
required-check protection, package access, and automatic GitHub mirroring remain unverified.
Workflow files alone do not configure these server settings. Forgejo package publishing and
downloads must pass before the package migration can be marked complete.
