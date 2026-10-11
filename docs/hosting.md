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

## Packages

Meta publishes to GitHub Packages at `https://maven.pkg.github.com/reazndev/otus-meta`. A version
tag starts the `publish` workflow on Linux. It runs `checkAll`, builds every publication in an
isolated local Maven repository, verifies the root metadata, platform binaries, and checksums,
and then uploads with the workflow's `GITHUB_TOKEN`. A manual run does the same checks without
uploading. See [releasing](releasing.md).

GitHub Packages has versions up to 0.3.0 and from 0.6.0 on. Versions 0.3.1 to 0.5.0 are not
on GitHub Packages.

Downloading needs a token, including in CI. Each app stores a GitHub token with
`read:packages` as the `OTUS_META_PACKAGES_TOKEN` Actions secret. See [consuming](consuming.md).
