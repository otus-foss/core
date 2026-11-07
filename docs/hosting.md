# Hosting and CI

## Repositories

GitHub hosts all three repositories. Develop, open pull requests, and file issues there:

- [Otus Mail](https://github.com/reazndev/otus-mail)
- [Otus Calendar](https://github.com/reazndev/otus-calendar)
- [Otus Meta](https://github.com/reazndev/otus-meta)

Checkouts use `origin` for GitHub.

## Workflow configuration

`.github/workflows` holds every workflow. Linux jobs run on Ubicloud's `ubicloud-standard-2`
runners. The `ios` quality jobs and their `runs-on: macos-15` label stay in the files, but
`if: false` pauses them. Delete that condition to run them again. Until then, CI does not link
iOS apps or run simulator tests.

`actionlint` lints workflows in pull requests that change `.github/`. `issue link` warns when a
pull request closes no issue, and `issue report` lists each Monday the open issues that merged pull
requests name, in an issue called "Open issues with merged work". Dependabot opens one grouped
pull request a week for GitHub Actions and one for Gradle, leaving the pinned Otus libraries alone.
The `coverage` workflow runs the JVM tests with Kover and comments a coverage summary; see
[coverage](quality.md#coverage).

Pull request workflows run once, when a pull request opens as ready or a draft is marked ready.
Drafts run nothing. Later pushes run them again only while the pull request has the
`ci on every push` label, and adding the label runs them at once.

The `land` workflow merges a pull request once a maintainer adds `ready to land`, together with
its partners in the other repositories. See [landing](landing.md).

Each `quality` run first lists the changed files: workflow-only and Markdown-only pull requests skip
the build, and pull requests that change only Android code skip the macOS `ios` job. The
`pull request` workflow labels each pull request by size (`size:XS` to `size:XL`, tests left
out), comments the screenshots it adds or changes beside the old ones, and warns when UI code
changes without a screenshot.

## Packages

Meta publishes to GitHub Packages at `https://maven.pkg.github.com/reazndev/otus-meta`.
**Publishing is paused:** the `publish` workflow runs only when started by hand, and a manual run
never uploads, so a version tag publishes nothing until the tag trigger returns to
`.github/workflows/publish.yml`. With that trigger, a version tag on `main` starts the workflow on
Linux. Its `build` job runs the modules' host tests,
builds every publication in an isolated local Maven repository, and verifies the root metadata,
platform binaries, checksums, and `com.otus` dependency versions. Its `publish` job then uploads
with the workflow's `GITHUB_TOKEN`. The workflow does not run `checkAll`; run it by hand before tagging.
A manual run does the same checks without uploading. See [releasing](releasing.md).

GitHub Packages has versions up to 0.3.0 and from 0.6.0 on. Versions 0.3.1 to 0.5.0 are not
on GitHub Packages.

Downloading needs a token, including in CI. Each app stores a GitHub token with
`read:packages` as the `OTUS_META_PACKAGES_TOKEN` Actions secret. See [consuming](consuming.md).
