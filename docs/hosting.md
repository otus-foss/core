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

The Forgejo workflows retain portable, Android, and iOS checks. Action URLs explicitly select
the pinned GitHub action commits. They use the Gradle wrapper directly and do not require an
Actions cache service. Neither iOS checks nor publishing have been verified on Forgejo yet.

Register Forgejo Runner 7 or newer and provide these labels and environments:

- `otus-linux`: a Linux execution environment with Git, Bash, Node.js 20 for the pinned
  JavaScript actions, and the system libraries required by Temurin 21, Python 3.13, and the
  Android command-line tools. Actions install the JDK, Python, Android platform 37.0, and
  build-tools 36.0.0. Host Android tests also need the native graphics libraries used by
  Robolectric. Use a maintained runner image that provides those libraries.
- `otus-macos`: a macOS 15 Apple Silicon host with Git, Bash, Node.js 20, Homebrew, and Xcode
  26.3 at `/Applications/Xcode_26.3.app`. It must allow `sudo xcode-select`. Actions install
  Temurin 21, Python 3.13 for publishing, Android command-line tools for publishing, and Mint
  for the apps' pinned Swift tools.

The labels are requirements for runner registration, not a claim that those runners exist.
Enable Actions in each repository and require the resulting `portable`, `android`, and `ios`
statuses on `main` after their names and successful execution are verified. Keep existing
required checks intact; do not bypass them to complete the migration.

## Packages

Released libraries remain on GitHub Packages during the Git cutover. Mail's default registry
and GitHub publishing are unchanged. GitHub read tokens remain necessary for those artifacts.
No library version or application dependency is changed by the hosting migration.

Meta adds `publishAllPublicationsToForgejoPackagesRepository` for the Maven registry at
`https://git.hilfikers.com/api/packages/florian.ruby/maven`. Its Gradle credentials are
`ForgejoPackagesUsername` and `ForgejoPackagesPassword`. Store a Forgejo package token as
`FORGEJO_PACKAGES_TOKEN` in Meta's Forgejo Actions secrets. SSH keys cannot authenticate Maven
HTTP requests. The prepared Forgejo publishing workflow requires that token and a Mac runner.
It checks and builds every publication before uploading.

For Mail's Forgejo CI while using GitHub Packages, configure:

- `OTUS_META_PACKAGES_TOKEN`: the existing GitHub token with `read:packages`.
- `OTUS_META_PACKAGES_USERNAME`: an Actions variable containing the GitHub username, defaulting
  to `reazndev`. A Forgejo username is not a GitHub package credential.

After complete artifacts for the pinned version are available and tested on Forgejo, set Mail's
`OTUS_META_REGISTRY_URL` Actions variable to the Forgejo Maven URL and replace those credentials
with a Forgejo username and package-read token. Local builds select that registry with
`otusMetaRegistry` in private Gradle properties. See [consuming.md](consuming.md).
Validate a clean Android build and iOS linking before changing the default registry. Do not
republish a released version from changed sources or publish only its Android variants.

## Unverified server settings

SSH Git access was tested for all three repositories. Web and API access require sign-in,
which was unavailable during the cutover. Runner availability, Actions enablement, secrets,
required-check protection, package access, and automatic GitHub mirroring remain unverified.
Workflow files alone do not configure these server settings. Future releases still need the
working GitHub publishing path until Forgejo publishing passes.
