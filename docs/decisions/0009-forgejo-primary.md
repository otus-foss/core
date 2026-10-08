# 0009: Forgejo as the primary Git host

Status: accepted by the user on 2026-10-08.

## Decision

The three Otus repositories move to `git.hilfikers.com/florian.ruby` as their primary Git host.
Development, pull requests, and new issues use Forgejo. Existing checkouts name this remote
`origin` and retain GitHub as `github`. The repositories remain independent.

Git hosting moves immediately. CI runners, secrets, branch protection, and package publishing
require separate authenticated server configuration. Retain the working GitHub workflows and
GitHub Packages releases while those Forgejo services are configured and validated. This
changes the host in decision 0005 without changing library coordinates or versioning.

## Consequences

- Preserve history with normal fast-forward pushes. Never overwrite changes on either host.
- Keep `.forgejo/workflows` and `.github/workflows` separate so each host runs its own workflow.
- A secondary Git remote does not synchronize repositories, issues, or settings automatically.
- Prepare Forgejo Maven publishing and a configurable Mail registry, but keep released
  dependency resolution working until complete Forgejo artifacts are verified.
- Record implemented Git changes and unverified server requirements in [hosting.md](../hosting.md).
