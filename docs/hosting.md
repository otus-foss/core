# Hosting and CI

This guide covers all three repositories. Each app's own `docs/hosting.md` adds only what is
particular to it, such as Mail's nightly builds.

## Repositories

GitHub hosts all three repositories. Develop, open pull requests, and file issues there:

- [Otus Mail](https://github.com/otus-foss/mail)
- [Otus Calendar](https://github.com/otus-foss/calendar)
- [Otus Core](https://github.com/otus-foss/core)

Checkouts use `origin` for GitHub. Clone the apps next to Core, because they build it from
source; see [consuming](consuming.md):

```sh
git clone git@github.com:otus-foss/core.git
git clone git@github.com:otus-foss/mail.git
git clone git@github.com:otus-foss/calendar.git
(cd core && git config --local core.hooksPath .githooks)
(cd mail && git config --local core.hooksPath .githooks)
(cd calendar && git config --local core.hooksPath .githooks)
```

## Workflows

`.github/workflows` holds each repository's workflows. Linux jobs run on GitHub's `ubuntu-24.04`
runners, pinned so the image under the screenshot tests changes only on purpose. The shared
`setup-build` action first deletes toolchains the builds don't use, because the image leaves about
14 GB free. The `ios` jobs run on GitHub's `macos-15` runners with Xcode 26.3, and only while the
repository variable `IOS_CI` is `on` (Settings › Secrets and variables › Actions › Variables).
Leave it unset to save macOS minutes.

- `quality` runs `checkAll` as parallel jobs, plus the `ios` job; see [quality](quality.md).
- `coverage` runs the JVM tests with Kover and comments a coverage summary; see
  [coverage](quality.md#coverage).
- `pull request` labels each pull request by size (`size:XS` to `size:XL`, tests left out),
  comments the screenshots it adds or changes beside the old ones, and warns when UI code changes
  without a screenshot. In the apps it also adds a `needs core` label when core has a branch
  with the same name, since that pair has to land together, Core first.
- `actionlint` lints workflows in pull requests that change `.github/`.
- `issue link` warns when a pull request closes no issue, and `issue report` lists each Monday the
  open issues that merged pull requests name, in an issue called "Open issues with merged work".
- `land` merges a pull request once a maintainer adds `ready to land`, together with its partners
  in the other repositories; see [landing](landing.md). It runs on label changes and pushes, and
  on every finished check workflow, and checks out no code, so it needs no ready gate.

Dependabot opens one grouped pull request a week each for GitHub Actions, Gradle, and the pinned
Python tools. The apps' Gradle updates cover only their `gradle/app.versions.toml`; the shared
versions update here, in Core's catalog.

## When workflows run

Pull request workflows run once, when a pull request opens as ready or a draft is marked ready.
Drafts run nothing. Later pushes run them again only while the pull request has the
`ci on every push` label, and adding the label runs them at once. `quality` and `coverage` also run
on every push to `main`, so each merge is checked again, and those runs never cancel each other.

The rule lives once, in the `ready-gate` action. Each workflow starts with a small job, or for
`quality` the `changes` job, that asks it, and the other jobs need that job. `quality` then lists
the changed files: workflow-only and Markdown-only pull requests skip the build, and pull requests
that change only Android code skip the macOS `ios` job.

## Shared actions

Core's `.github/actions` holds the steps every repository repeats. The apps check out core
and use the actions from that checkout with `uses: ./core/.github/actions/<name>`, so each
job runs the actions of the Core branch it builds with; Core uses `./.github/actions/<name>`. Only
the apps' `land` workflow calls Core directly, `lander.yml` from `main`, which needs Core's Actions
access setting; see [landing](landing.md#setup).

- `ready-gate` says whether the run should check anything.
- `quality-changes` lists the changed files and says which `quality` jobs they need.
- `setup-build` installs Java, Gradle, the Android SDK packages, and the pinned Python tools.
- `size-label` labels a pull request by size.
- `screenshot-comment` comments the screenshots a pull request adds or changes.

Every app job checks core out with its full history and first switches it to the branch with
the app's branch name when there is one, so every action it then runs comes from that branch. An
action change is therefore tested by its partner app pull request, and an app pull request runs
before its Core partner merges.

An app's build jobs check the app out to `otus-<app>` and Core to `core`, side by side as on
a development machine, so Gradle finds Core at `../core` and Spotless never sees Core's
files. The repositories are public, so the checkouts need no key, and Dependabot's pull requests
run the same checks. Core's architecture check fails a workflow that gives a repository
secret to a Gradle step or shares one through a workflow or job `env`.

## Packages

Core publishes no packages; the apps build it from source
([decision 0020](decisions/0020-no-library-publishing.md)). GitHub Packages still holds the old
versions, up to 0.3.0 and from 0.6.0 on, and gets no new ones.
