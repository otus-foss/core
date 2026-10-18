# 0013: Local hooks as the quality gate

Status: accepted by the user on 2026-10-11.

## Context

The three repositories are private on a GitHub plan that cannot protect branches or require
checks, so CI reported results but blocked nothing. Each pull request ran `quality` twice, once
for the pull request and again for the push to `main`. Each run used two Linux jobs, `portable`
and `android`, although `android` already ran `checkAll`, which includes `qualityCheck`. Meta's
`publish` workflow ran `checkAll` again on a commit that had already passed. Ubicloud bills every
runner minute.

## Decision

- Each repository versions a `pre-commit` hook that runs the Spotless and Ruff checks, and a
  `pre-push` hook that runs `checkAll` for every push that sends commits, including tags.
- The `quality` workflow runs one Linux `check` job with `checkAll`. It runs only on pull requests
  that change files other than Markdown. A push to `main` no longer starts it.
- Meta's pull request check no longer builds and verifies publications. The `publish` workflow
  still does, and it no longer runs `checkAll`.
- The paused `ios` jobs stay in the workflows.

## Consequences

- The hooks are the only check on a direct push to `main`. A checkout without
  `core.hooksPath .githooks`, or a push with `--no-verify`, goes unchecked.
- The hooks check the working tree rather than the pushed commits, so uncommitted changes can make
  a local result differ from CI.
- A pull request that changes only Markdown gets no CI run. The `pre-commit` Spotless check still
  covers Markdown whitespace.
- A library change that breaks publication shows up when publishing. Run `publish` manually on
  the branch before tagging a release that changes the build.
- This replaces the rule in each app's quality guide that local hooks are not relied upon.

## Amendment: one run per ready pull request

Accepted by the user on 2026-10-18, after CI had been paused to manual runs.

- `quality` runs once per pull request: when a pull request that is not a draft opens, or when a
  draft is marked ready for review. Later pushes to the branch do not start it again.
- Open pull requests as drafts and mark them ready once every commit is pushed. Start the workflow
  by hand to check again.
- Mail's `check` job builds against Meta's source, from the Meta branch with the same name when one
  exists and from `main` otherwise, because Meta publishing is paused.
