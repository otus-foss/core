# 0019: Landing partner pull requests together

## Status

Proposed on 2026-11-07. Takes effect once merged and the GitHub App from
[landing](../landing.md#setup) is set up.

## Context

A Meta change and the Mail change that adopts it are two pull requests in two repositories, and
GitHub cannot merge them as one. Merged by hand, they can land hours apart. The nightly build pairs
Mail `main` with Meta `main`, so nightly 126 shipped Meta's new `QuickJump` slot without the Mail
change, and the inbox pull-down slowed down. The repositories will become public, so approval must
come from a maintainer and not from anyone who can open a pull request.

## Decision

- A maintainer approves a pull request with the `ready to land` label. The approval is the
  `otus/land` status on the head commit, and a new push removes the label.
- Open pull requests from a branch with the same name in `otus-meta`, `otus-mail`, and
  `otus-calendar` form a group. The lander merges the group, Meta first, once every member is
  approved, green, conflict-free, and not blocked by a review.
- A GitHub App token does the work, because the default token cannot reach other repositories.
- Once the repositories are public, a ruleset on `main` requires the `otus/land` status.

## Consequences

- The merge button is no longer the way to land a pull request. Until rulesets apply, it still
  works and skips the partner.
- Fork pull requests never pair. A cross-repository change from outside lands one pull request at
  a time.
- If a later merge in a group fails, the earlier ones stay merged, and the lander reports it.
