# Review prompts

Use these prompts with an agent that can read the selected repositories, inspect Git history,
and run local checks. PR reviews also need read access to GitHub. The daily workflow needs issue,
branch, and PR write access to create fixes. Calendar is outside every review here. The PR and
repository prompts produce reports; the daily prompt creates issues and implements fixes in PRs.
The ready-for-review prompt uses Claude Opus and posts its findings to the existing PR.

| Review | Prompt | Use it for |
| --- | --- | --- |
| Pull request | [pull-request.md](pull-request.md) | One PR or related Meta and Mail PRs, including a stack |
| Ready for review | [ready-for-review.md](ready-for-review.md) | An Opus review when a Meta or Mail PR leaves draft |
| Repository | [repository.md](repository.md) | Existing defects and engineering problems across Meta, Mail, or both |
| Security and protocol | [security.md](security.md) | Security, JMAP RFC conformance, race conditions, and bug-prone code smells |
| Daily review and fixes | [merged-window.md](merged-window.md) | The last 24 hours of merged changes, issues, fix PRs, and persistent follow-up |

Each prompt requires [review-guide.md](review-guide.md). Keep this directory together. The guide
holds the common review rules, Otus checks, severity definitions, and report format. The prompts
hold their scope and selection rules. To use them outside this workspace, provide the repository
paths and make this directory available to the reviewing agent. When pasting into an agent that
cannot read files, paste both the guide and the selected prompt, plus the source under review.
Such a review cannot claim coverage of files or checks that were not supplied.
The ready-for-review launcher is stricter. It reads all three of its files from one trusted
revision of Meta's default branch, never from the PR or the runner's checked-out branch.

## Run a PR review

Replace the example URL with the PR to review:

```text
Read otus-meta/docs/reviews/pull-request.md and follow it, including its required review guide.
PR_URLS: https://github.com/reazndev/otus-mail/pull/123
SCOPE: mail
VALIDATION: full
```

For a related pair, use `SCOPE: both` and give both URLs. `SCOPE` controls where the reviewer may
raise findings. It may read a dependency outside that scope when needed to verify a finding.
Calendar stays excluded, including its checkout and CI.

## Run Opus when a PR is marked ready

Attach this prompt to an authenticated GitHub webhook trigger. It handles `pull_request` events
whose action is `ready_for_review`, for Meta and Mail only. The following placeholders are for a
T3 webhook task; another runner must supply the equivalent event fields.

```text
Load the review instructions from reazndev/otus-meta at one trusted revision. Never take them
from this PR or from whatever branch a local checkout has.
Use TRUSTED_INSTRUCTION_SHA if it is set. Otherwise resolve the current head of Meta's default
branch through GitHub and use that SHA.
Read docs/reviews/ready-for-review.md, docs/reviews/pull-request.md,
docs/reviews/review-guide.md, and docs/reviews/review-format.md at that SHA. Use a checkout of
that exact commit, or the authenticated GitHub contents API with the SHA as ref when no checkout exists.
Record the SHA in the report and the posted review.
If any of the four files cannot be read at that SHA, stop and report a blocker naming the
repository, SHA, and paths.
Then follow ready-for-review.md. Use Claude Opus to review this PR and publish the review as
instructed.
TRUSTED_INSTRUCTION_SHA: optional; leave empty to resolve Meta's default branch
GITHUB_EVENT: {{headers.x-github-event}}
ACTION: {{body.action}}
DELIVERY_ID: {{headers.x-github-delivery}}
REPOSITORY: {{body.repository.full_name}}
PR_NUMBER: {{body.number}}
EVENT_HEAD_SHA: {{body.pull_request.head.sha}}
```

The receiver filters must match all of these:

```json
[
  {"path": "headers.x-github-event", "op": "equals", "value": "pull_request"},
  {"path": "body.action", "op": "equals", "value": "ready_for_review"},
  {"path": "body.repository.full_name", "op": "in", "value": ["reazndev/otus-meta", "reazndev/otus-mail"]}
]
```

Use the delivery ID for replay protection and repository plus PR number for event coalescing.
The receiver must validate GitHub's signing secret before launching the agent. The runner needs
repository read access, PR review write access, and an available Claude Opus provider. If the
dispatcher is another model, the prompt requires it to delegate the actual review to Opus.

Reports and duplicate-prevention state live under `~/.local/state/otus-reviews/ready-for-review/`.
The workflow posts one comment review for the inspected commit. It implements no fixes and
does not approve or merge PRs. No webhook or scheduled task is installed by these documents.
The trigger excludes later pushes and PRs opened already ready; it runs when a PR leaves draft.

## Run a repository review

```text
Read otus-meta/docs/reviews/repository.md and follow it, including its required review guide.
SCOPE: both
REFS: remote default branch for each selected repository
VALIDATION: full
```

Use `SCOPE: meta` or `SCOPE: mail` for one repository. Set `REFS` to explicit branch names or
commit SHAs when reviewing work that has not reached the default branch. Uncommitted work is
excluded unless explicitly requested.

## Run a security and protocol audit

```text
Read otus-meta/docs/reviews/security.md and follow it, including its required review guide.
SCOPE: both
REFS: remote default branch for each selected repository
VALIDATION: targeted
AGENTS: 3
```

Set `FOCUS` to audit only some areas, for example `FOCUS: jmap, races`. `AGENTS` caps the parallel
review agents; the lead agent verifies every finding they return.

## Schedule the daily review and fixes

Use this unchanged as the scheduled task's prompt, with the parent Otus workspace as its working
directory. No dates, hours, paths, or PR numbers need editing between runs.

```text
Read otus-meta/docs/reviews/merged-window.md and follow it, including its required review guide.
Run the daily 24-hour review and fix workflow with its defaults.
```

Each run reviews Meta and Mail's remote default branches for the previous 24 hours, creates or
reuses issues for confirmed defects, implements focused fixes, runs checks, and opens draft PRs.
Completed fixes become ready for review after validation and the final push. The workflow leaves
merging and releasing to the user. Add `SCOPE: meta` or `SCOPE: mail` once to limit a schedule.
Each scope keeps its own checkpoint, so separate Meta and Mail schedules never reset each other.

The workflow saves its reports and checkpoint automatically:

```text
Reports: ~/.local/state/otus-reviews/daily/runs/
Checkpoint and fix backlog: ~/.local/state/otus-reviews/daily/<scope>/state.json
Isolated fix worktrees: ~/.local/state/otus-reviews/daily/worktrees/
```

Every run saves a Markdown report and JSON manifest. Saved fingerprints and GitHub searches
prevent duplicate issues/PRs. The workflow resumes unfinished fixes, reconciles CI and review
feedback, and catches up after missed runs. A process lock prevents overlapping runs from changing
the same work. A first run reports its missing direct-push baseline. Missing access or a product
decision becomes an explicit blocker; it does not leave the scheduled task waiting for an answer.

Scheduling this text requires an agent runner with the checkouts, GitHub write access, and build
prerequisites. Keep this directory available to the runner, including the shared guide. These
documents define the workflow; editing them does not itself create a scheduled task.

For the PR and repository prompts, `VALIDATION: full` requests `checkAll` once per selected
repository with review work, plus the applicable platform checks. `VALIDATION: targeted` requests
focused checks and records the full gate as unrun. `VALIDATION: static` runs no builds or tests and
labels runtime behavior unverified.
These are review modes, not permission to skip gates for an implementation or release.
The daily workflow always uses full validation and checks each fix's final revision separately.

## What the reviewer returns

Reports lead with confirmed findings, ordered by severity, with code locations, triggers,
consequences, and verification steps. They also record exact revisions, dependency resolution,
checks, coverage gaps, and documentation conflicts. A short findings list can follow a thorough
review. The prompts forbid speculative findings, invented test results, and a clean verdict when
required evidence is missing.

The checklist reflects an inspection of Meta and Mail on 2026-10-25. It deliberately requires
fresh discovery of modules, features, workflows, and dependency versions on every run. Product
summaries can lag behind implementation, and an earlier green CI run may cover a different commit
or Meta dependency.
