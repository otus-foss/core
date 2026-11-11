# 0012: GitHub as the primary host again

Status: accepted by the user on 2026-10-11. Replaces [decision 0009](0009-forgejo-primary.md).

## Decision

The three Otus repositories move back to `github.com/reazndev` as their only Git, CI, and package
host. Development, pull requests, issues, Actions, and Maven packages use GitHub. Checkouts name
GitHub `origin`. Forgejo stays reachable as the `forgejo` remote for history only.

Before the switch, every Forgejo branch and tag was pushed to GitHub. Forgejo had no open pull
requests. The `.forgejo` workflows and the Forgejo Maven repository are removed.

Linux jobs run on Ubicloud's `ubicloud-standard-2` runners. macOS jobs are paused with
`if: false` and keep their `runs-on: macos-15` label. Meta publishes from Linux as decision 0011
describes, now to GitHub Packages.

## Consequences

- Push only to GitHub. Do not push new work to Forgejo.
- Mail resolves Meta from GitHub Packages. Versions 0.3.1 to 0.5.0 exist only in Forgejo Packages
  and are not copied. The first release published to GitHub after the switch is 0.6.0.
- iOS linking and simulator tests do not run in CI until the macOS jobs are enabled again. They
  were enabled again on 2026-11-04, on GitHub's `macos-15` runners.
- Linux jobs moved from Ubicloud to GitHub's `ubuntu-24.04` runners on 2026-11-11, ahead of the
  repositories going public, where GitHub's runners are free.
