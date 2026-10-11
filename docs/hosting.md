# Hosting and CI

## Repositories

GitHub hosts all three repositories. Develop, open pull requests, and file issues there:

- [Otus Mail](https://github.com/reazndev/otus-mail)
- [Otus Calendar](https://github.com/reazndev/otus-calendar)
- [Otus Meta](https://github.com/reazndev/otus-meta)

The repositories used Forgejo at `git.hilfikers.com/florian.ruby` from 2026-10-08 to
2026-10-11. See [decision 0012](decisions/0012-github-primary.md). Every Forgejo branch and tag
was pushed to GitHub before the switch, so GitHub holds the complete history.

Checkouts use `origin` for GitHub. A checkout that still has Forgejo as `origin` can switch with
these commands, run from the repository and with `<repo>` replaced by its name:

```sh
git remote rename origin forgejo
git remote rename github origin  # or: git remote add origin git@github.com:reazndev/<repo>.git
git fetch origin
git branch --set-upstream-to=origin/main main
```

Do not push new work to Forgejo.

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

GitHub Packages has versions up to 0.3.0 and the releases published after the switch.
Versions 0.3.1 to 0.5.0 were published only to Forgejo and are not on GitHub.

Downloading needs a token, including in CI. Each app stores a GitHub token with
`read:packages` as the `OTUS_META_PACKAGES_TOKEN` Actions secret. See [consuming](consuming.md).
